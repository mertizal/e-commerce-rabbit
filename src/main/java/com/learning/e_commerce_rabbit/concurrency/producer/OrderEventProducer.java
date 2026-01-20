package com.learning.e_commerce_rabbit.concurrency.producer;

import com.learning.e_commerce_rabbit.concurrency.config.RabbitTopicConfig;
import com.learning.e_commerce_rabbit.concurrency.domain.Order;
import com.learning.e_commerce_rabbit.concurrency.event.OrderCreatedEventV2;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.MessageDeliveryMode;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(Order order) {

        OrderCreatedEventV2 event =
                new OrderCreatedEventV2(
                        UUID.randomUUID().toString(),
                        order.getId(),
                        order.getPrice(),
                        order.isPremium(),
                        order.getCampaignCode()
                );

        int priority = order.isPremium() ? 8 : 1;

        log.info("Publishing OrderCreatedEvent | orderId={} | premium={} | priority={}",
                order.getId(), order.isPremium(), priority);

        CorrelationData correlationData =
                new CorrelationData(UUID.randomUUID().toString());

        // Order event (OrderCreatedConsumer için)
        rabbitTemplate.convertAndSend(
                RabbitTopicConfig.ORDER_EXCHANGE,
                RabbitTopicConfig.ORDER_CREATED_ROUTING_KEY,
                event,
                message -> {
                    message.getMessageProperties()
                            .setDeliveryMode(MessageDeliveryMode.PERSISTENT);

                    message.getMessageProperties().setPriority(priority);

                    // BİLEREK V2 GÖNDERİYORUZ
                    message.getMessageProperties().setHeader("event-type", "order.created");
                    message.getMessageProperties().setHeader("event-version", 2);

                    return message;
                },
                correlationData
        );

    }
}