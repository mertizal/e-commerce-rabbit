package com.learning.e_commerce_rabbit.concurrency.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentRetryConfig {

    public static final String PAYMENT_RETRY_EXCHANGE = "payment.retry.exchange";

    public static final String PAYMENT_RETRY_1_QUEUE = "payment.retry.1.queue";
    public static final String PAYMENT_RETRY_2_QUEUE = "payment.retry.2.queue";
    public static final String PAYMENT_RETRY_3_QUEUE = "payment.retry.3.queue";

    public static final String PAYMENT_RETRY_1_ROUTING_KEY = "payment.retry.1";
    public static final String PAYMENT_RETRY_2_ROUTING_KEY = "payment.retry.2";
    public static final String PAYMENT_RETRY_3_ROUTING_KEY = "payment.retry.3";

    @Bean
    public DirectExchange paymentRetryDirectExchange() {
        return new DirectExchange(PAYMENT_RETRY_EXCHANGE);
    }

    @Bean
    public Queue paymentRetryOneQueue() {
        return QueueBuilder.durable(PAYMENT_RETRY_1_QUEUE)
                .withArgument("x-message-ttl", 5000)
                .withArgument("x-dead-letter-exchange", PaymentRabbitConfig.PAYMENT_EXCHANGE)
                .withArgument("x-dead-letter-routing-key",
                        PaymentRabbitConfig.PAYMENT_REQUESTED_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue paymentRetryTwoQueue() {
        return QueueBuilder.durable(PAYMENT_RETRY_2_QUEUE)
                .withArgument("x-message-ttl", 15000)
                .withArgument("x-dead-letter-exchange", PaymentRabbitConfig.PAYMENT_EXCHANGE)
                .withArgument("x-dead-letter-routing-key",
                        PaymentRabbitConfig.PAYMENT_REQUESTED_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue paymentRetryThirdQueue() {
        return QueueBuilder.durable(PAYMENT_RETRY_3_QUEUE)
                .withArgument("x-message-ttl", 45000)
                .withArgument("x-dead-letter-exchange", PaymentRabbitConfig.PAYMENT_EXCHANGE)
                .withArgument("x-dead-letter-routing-key",
                        PaymentRabbitConfig.PAYMENT_REQUESTED_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding paymentRetryOneBinding() {
        return BindingBuilder
                .bind(paymentRetryOneQueue())
                .to(paymentRetryDirectExchange())
                .with(PAYMENT_RETRY_1_ROUTING_KEY);
    }

    @Bean
    public Binding paymentRetryTwoBinding() {
        return BindingBuilder
                .bind(paymentRetryTwoQueue())
                .to(paymentRetryDirectExchange())
                .with(PAYMENT_RETRY_2_ROUTING_KEY);
    }

    @Bean
    public Binding paymentRetryThirdBinding() {
        return BindingBuilder
                .bind(paymentRetryThirdQueue())
                .to(paymentRetryDirectExchange())
                .with(PAYMENT_RETRY_3_ROUTING_KEY);
    }
}

