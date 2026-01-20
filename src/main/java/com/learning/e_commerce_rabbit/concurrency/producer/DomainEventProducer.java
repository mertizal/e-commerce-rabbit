package com.learning.e_commerce_rabbit.concurrency.producer;

import com.learning.e_commerce_rabbit.concurrency.config.RabbitTopicConfig;
import com.learning.e_commerce_rabbit.concurrency.domain.Order;
import com.learning.e_commerce_rabbit.concurrency.event.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DomainEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getPrice(),
                order.isPremium()
        );


        rabbitTemplate.convertAndSend(
                RabbitTopicConfig.ORDER_EXCHANGE,
                RabbitTopicConfig.ORDER_CREATED_ROUTING_KEY,
                event,
                message -> {
                    message.getMessageProperties()
                            .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                }
        );

        log.info("OrderCreatedEvent published for order {}", order.getId());
    }
}
