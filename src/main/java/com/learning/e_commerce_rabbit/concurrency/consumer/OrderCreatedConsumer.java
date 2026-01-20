package com.learning.e_commerce_rabbit.concurrency.consumer;

import com.learning.e_commerce_rabbit.concurrency.config.PaymentRabbitConfig;
import com.learning.e_commerce_rabbit.concurrency.config.RabbitTopicConfig;
import com.learning.e_commerce_rabbit.concurrency.event.OrderCreatedEventV2;
import com.learning.e_commerce_rabbit.concurrency.event.PaymentRequestedEvent;
import com.learning.e_commerce_rabbit.concurrency.event.ProcessedEvent;
import com.learning.e_commerce_rabbit.concurrency.exception.BusinessRuleViolationException;
import com.learning.e_commerce_rabbit.concurrency.exception.ExternalServiceException;
import com.learning.e_commerce_rabbit.concurrency.exception.MessageParseException;
import com.learning.e_commerce_rabbit.concurrency.metrics.ConsumerMetrics;
import com.learning.e_commerce_rabbit.concurrency.metrics.OrderMetrics;
import com.learning.e_commerce_rabbit.concurrency.repository.ProcessedEventRepository;
import com.learning.e_commerce_rabbit.concurrency.rpc.StockRpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderCreatedConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final StockRpcClient stockRpcClient;
    private final OrderMetrics orderMetrics;
    private final ConsumerMetrics consumerMetrics;
    private final ProcessedEventRepository processedEventRepository;

    @RabbitListener(
            queues = RabbitTopicConfig.ORDER_CREATED_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    @Transactional
    public void consume(Message message) {

        consumerMetrics.incrementOrderCreatedConsumed();

        // Parse message
        OrderCreatedEventV2 event = parseMessage(message);

        String eventId = event.getOrderId(); // veya message header’dan UUID

        // Idempotency check
        if (processedEventRepository.existsById(eventId)) {
            log.warn(
                    "DUPLICATE EVENT IGNORED | eventId={}",
                    eventId
            );
            consumerMetrics.incrementDuplicateIgnored();
            return;
        }


        try {
            Integer priority = message.getMessageProperties().getPriority();

            log.info(
                    "ORDER RECEIVED | orderId={} | priority={} | campaign={}",
                    event.getOrderId(),
                    priority,
                    event.getCampaignCode()
            );

            // Stock check
            boolean stockAvailable = checkStock(event.getOrderId());

            if (!stockAvailable) {
                log.warn("STOCK NOT AVAILABLE | orderId={}", event.getOrderId());
                orderMetrics.incrementStockRejected();

                // Fatal error → Error handler NACK yapacak → DLQ
                throw new BusinessRuleViolationException(
                        "Stock not available for order: " + event.getOrderId()
                );
            }

            // Publish payment event
            publishPaymentEvent(event, priority);

            // Mark as processing (DB write)
            processedEventRepository.save(
                    new ProcessedEvent(
                            eventId
                    )
            );

            // Success
            consumerMetrics.incrementOrderCreatedSuccess();
            log.info("ORDER SUCCESS | orderId={}", event.getOrderId());
        } catch (Exception ex) {
            throw ex;
        }

        // Spring otomatik ACK yapacak (exception yok)
    }

    private OrderCreatedEventV2 parseMessage(Message message) {
        try {
            return (OrderCreatedEventV2) rabbitTemplate
                    .getMessageConverter()
                    .fromMessage(message);
        } catch (Exception e) {
            throw new MessageParseException("Failed to parse OrderCreatedEventV2", e);
        }
    }

    private boolean checkStock(String orderId) {
        try {
            return stockRpcClient.checkStock(orderId);
        } catch (Exception e) {
            // RPC timeout → Transient error (ama Order'da retry yok, direkt fail)
            throw new ExternalServiceException("Stock service timeout", e);
        }
    }

    private void publishPaymentEvent(OrderCreatedEventV2 event, Integer priority) {
        PaymentRequestedEvent paymentEvent =
                new PaymentRequestedEvent(
                        event.getEventId(),event.getOrderId(), event.getPrice());

        CorrelationData correlationData =
                new CorrelationData("payment-" + event.getEventId());

        rabbitTemplate.convertAndSend(
                PaymentRabbitConfig.PAYMENT_EXCHANGE,
                PaymentRabbitConfig.PAYMENT_REQUESTED_ROUTING_KEY,
                paymentEvent,
                msg -> {
                    msg.getMessageProperties()
                            .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    msg.getMessageProperties().setPriority(priority);

                    msg.getMessageProperties()
                            .setHeader("event-type", "payment.requested");
                    msg.getMessageProperties()
                            .setHeader("event-version", 1);

                    return msg;
                },
                correlationData
        );
    }
}