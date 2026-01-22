package com.learning.e_commerce_rabbit.concurrency.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConnectionConfig {


    @Value("${RABBITMQ_HOST:localhost}")
    private String rabbitHost;

    @Value("${RABBITMQ_PORT:5672}")
    private int rabbitPort;

    @Value("${RABBITMQ_USER:admin}")
    private String rabbitUser;

    @Value("${RABBITMQ_PASSWORD:admin}")
    private String rabbitPassword;

    @Bean
    public ConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(rabbitHost);
        factory.setPort(rabbitPort);
        factory.setUsername(rabbitUser);
        factory.setPassword(rabbitPassword);

        factory.setVirtualHost("/");

        factory.setConnectionTimeout(10_000);
        factory.setRequestedHeartBeat(30);
        factory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
        factory.setChannelCacheSize(25);
        factory.setChannelCheckoutTimeout(5_000);
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        factory.setPublisherReturns(true);

        com.rabbitmq.client.ConnectionFactory rabbitClientFactory = factory.getRabbitConnectionFactory();
        rabbitClientFactory.setAutomaticRecoveryEnabled(true);
        rabbitClientFactory.setTopologyRecoveryEnabled(true);
        rabbitClientFactory.setNetworkRecoveryInterval(5_000);

        return factory;
    }
}