package com.learning.e_commerce_rabbit.concurrency.config;


import org.springframework.amqp.core.*;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class StockRpcRabbitConfig {

    public static final String STOCK_RPC_EXCHANGE = "stock.rpc.exchange";
    public static final String STOCK_CHECK_REQUEST_QUEUE = "stock.check.request";
    public static final String STOCK_CHECK_REQUEST_ROUTING_KEY = "stock.check.request";

    // Reply queue'ya gerek yok! DirectReplyTo kullanılacak
    // public static final String STOCK_CHECK_REPLY_QUEUE = "stock.check.reply";

    @Bean
    public TopicExchange stockRpcExchange() {
        return new TopicExchange(STOCK_RPC_EXCHANGE, true, false);
    }

    @Bean
    public Queue stockCheckRequestQueue() {
        return QueueBuilder
                .durable(STOCK_CHECK_REQUEST_QUEUE)
                .build();
    }

    @Bean
    public Binding stockCheckRequestBinding() {
        return BindingBuilder
                .bind(stockCheckRequestQueue())
                .to(stockRpcExchange())
                .with(STOCK_CHECK_REQUEST_ROUTING_KEY);
    }

    @Bean(name = "stockRpcTemplate")
    public RabbitTemplate stockRpcTemplate(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setReplyTimeout(5000);
        rabbitTemplate.setExchange(STOCK_RPC_EXCHANGE);
        rabbitTemplate.setRoutingKey(STOCK_CHECK_REQUEST_ROUTING_KEY);
        rabbitTemplate.setUseDirectReplyToContainer(true); // DirectReplyTo
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory stockRpcListenerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO); // Auto ACK
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(5);
        factory.setObservationEnabled(true); // metrics için
        return factory;
    }
}

