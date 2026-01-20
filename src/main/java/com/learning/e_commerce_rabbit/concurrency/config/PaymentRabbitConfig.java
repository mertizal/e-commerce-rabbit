package com.learning.e_commerce_rabbit.concurrency.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentRabbitConfig {

    public static final String PAYMENT_EXCHANGE = "payment.exchange";

    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String PAYMENT_REQUESTED_ROUTING_KEY = "payment.requested";

    public static final String PAYMENT_DLX_EXCHANGE = "payment.dlx.exchange";
    public static final String PAYMENT_DLQ = "payment.dlq";

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange paymentDlxExchange() {
        return new DirectExchange(PAYMENT_DLX_EXCHANGE);
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX_EXCHANGE)
                .build(); // priority YOK
    }

    @Bean
    public Queue paymentDlq() {
        return QueueBuilder.durable(PAYMENT_DLQ).build();
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder
                .bind(paymentQueue())
                .to(paymentExchange())
                .with(PAYMENT_REQUESTED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentDlqBinding() {
        return BindingBuilder
                .bind(paymentDlq())
                .to(paymentDlxExchange())
                .with(PAYMENT_REQUESTED_ROUTING_KEY);
    }
}
