package com.learning.e_commerce_rabbit.concurrency.consumer;

import com.learning.e_commerce_rabbit.concurrency.config.PaymentRabbitConfig;
import com.learning.e_commerce_rabbit.concurrency.config.PaymentRetryConfig;
import com.learning.e_commerce_rabbit.concurrency.event.PaymentRequestedEvent;
import com.learning.e_commerce_rabbit.concurrency.event.ProcessedEvent;
import com.learning.e_commerce_rabbit.concurrency.exception.*;
import com.learning.e_commerce_rabbit.concurrency.metrics.ConsumerMetrics;
import com.learning.e_commerce_rabbit.concurrency.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final ProcessedEventRepository processedEventRepository;
    private final ConsumerMetrics consumerMetrics;
    private final Random random = new Random();

    @RabbitListener(
            queues = PaymentRabbitConfig.PAYMENT_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void consume(Message message) {

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Parse message
        PaymentRequestedEvent event = parseMessage(message);

        if (processedEventRepository.existsById(event.getEventId())) {
            log.warn(
                    "DUPLICATE PAYMENT EVENT SKIPPED | eventId={}",
                    event.getEventId()
            );
            return; // ACK → message drop
        }


        //  Retry count
        int retryCount = getRetryCountFromXDeath(message.getMessageProperties().getHeaders());

        log.info(
                "PAYMENT RECEIVED | orderId={} | amount={} | retryCount={}",
                event.getOrderId(),
                event.getAmount(),
                retryCount
        );

        // Validate
        validatePayment(event);

        // Process payment
        try {
            processPayment(event, retryCount);

            log.info("PAYMENT SUCCESS | orderId={} | retryCount={}", event.getOrderId(), retryCount);
            // Spring otomatik ACK yapacak
            processedEventRepository.save(
                    new ProcessedEvent(
                            event.getEventId()
                    )
            );


        } catch (ExternalServiceException e) {
            // TRANSIENT ERROR → Retry logic
            log.warn(
                    "⚠TRANSIENT ERROR | orderId={} | retryCount={} | error={}",
                    event.getOrderId(),
                    retryCount,
                    e.getMessage()
            );

            // Retry queue'ya gönder
            handleRetryOrDlq(retryCount, message);

            // Spring otomatik ACK yapacak (mesaj işlendi kabul edilir)
            // Error handler'a gitmesin diye exception fırlatmıyoruz
        }
    }

    private PaymentRequestedEvent parseMessage(Message message) {
        try {
            return (PaymentRequestedEvent) rabbitTemplate
                    .getMessageConverter()
                    .fromMessage(message);
        } catch (Exception e) {
            throw new MessageParseException("Failed to parse PaymentRequestedEvent", e);
        }
    }

    private void validatePayment(PaymentRequestedEvent event) {
        if (event.getOrderId() == null || event.getOrderId().isEmpty()) {
            throw new ValidationException("OrderId is required");
        }

        if (event.getAmount() <= 0) {
            throw new ValidationException("Amount must be positive: " + event.getAmount());
        }

        if (event.getAmount() > 100000) {
            throw new BusinessRuleViolationException("Amount exceeds limit: " + event.getAmount());
        }
    }

    private void processPayment(PaymentRequestedEvent event, int retryCount) {
        double randomValue = random.nextDouble();

        // 30% Transient error (gateway timeout)
        if (randomValue < 0.3) {
            throw new ExternalServiceException(
                    "Payment gateway timeout - attempt " + (retryCount + 1)
            );
        }

        // 10% Fatal error (card declined)
        if (randomValue < 0.4) {
            throw new BusinessRuleViolationException("Card declined - insufficient funds");
        }

        // 60% Success
        log.info("Payment processed successfully for order: {}", event.getOrderId());
    }

    private int getRetryCountFromXDeath(Map<String, Object> headers) {
        Object xDeathObj = headers.get("x-death");

        if (xDeathObj instanceof List<?> xDeathList && !xDeathList.isEmpty()) {
            Object first = xDeathList.get(0);
            if (first instanceof Map<?, ?> firstDeath) {
                Object count = firstDeath.get("count");
                if (count instanceof Long l) return l.intValue();
                if (count instanceof Integer i) return i;
            }
        }
        return 0;
    }

    private void handleRetryOrDlq(int retryCount, Message originalMessage) {

        MessageProperties newProps = MessagePropertiesBuilder
                .fromProperties(originalMessage.getMessageProperties())
                .build();

        Message retryMessage = MessageBuilder
                .withBody(originalMessage.getBody())
                .andProperties(newProps)
                .build();

        if (retryCount == 0) {
            log.info("→ RETRY-1 (5s)");
            rabbitTemplate.send(
                    PaymentRetryConfig.PAYMENT_RETRY_EXCHANGE,
                    PaymentRetryConfig.PAYMENT_RETRY_1_ROUTING_KEY,
                    retryMessage
            );
            return;
        }

        if (retryCount == 1) {
            log.info("→ RETRY-2 (15s)");
            rabbitTemplate.send(
                    PaymentRetryConfig.PAYMENT_RETRY_EXCHANGE,
                    PaymentRetryConfig.PAYMENT_RETRY_2_ROUTING_KEY,
                    retryMessage
            );
            return;
        }

        if (retryCount == 2) {
            log.info("→ RETRY-3 (45s)");
            rabbitTemplate.send(
                    PaymentRetryConfig.PAYMENT_RETRY_EXCHANGE,
                    PaymentRetryConfig.PAYMENT_RETRY_3_ROUTING_KEY,
                    retryMessage
            );
            return;
        }

        log.error("→ MAX RETRY EXCEEDED → DLQ");
        rabbitTemplate.send(
                PaymentRabbitConfig.PAYMENT_DLX_EXCHANGE,
                PaymentRabbitConfig.PAYMENT_REQUESTED_ROUTING_KEY,
                retryMessage
        );
    }
}