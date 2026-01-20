package com.learning.e_commerce_rabbit.concurrency.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConnectionConfig {

    @Bean
    public ConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory factory =
                new CachingConnectionFactory("localhost");

        // Credentials
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setVirtualHost("/");

        // ===============================
        // Connection settings
        // ===============================
        factory.setConnectionTimeout(10_000); // 10 sn
        factory.setRequestedHeartBeat(30);    // Broker ile heartbeat

        // ===============================
        // Channel caching (POOLING)
        // ===============================
        factory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);

        factory.setChannelCacheSize(25);
        // Aynı anda açık tutulabilecek channel sayısı

        factory.setChannelCheckoutTimeout(5_000);
        // Channel bulunamazsa ne kadar beklenecek

        // ===============================
        // Publisher confirms & returns
        // ===============================
        factory.setPublisherConfirmType(
                CachingConnectionFactory.ConfirmType.CORRELATED
        );
        factory.setPublisherReturns(true);

        // ===============================
        // Automatic recovery
        // ===============================
        com.rabbitmq.client.ConnectionFactory rabbitClientFactory =
                factory.getRabbitConnectionFactory();

        rabbitClientFactory.setAutomaticRecoveryEnabled(true);
        rabbitClientFactory.setTopologyRecoveryEnabled(true);

        rabbitClientFactory.setNetworkRecoveryInterval(5_000);

        return factory;
    }
}