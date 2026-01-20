package com.learning.e_commerce_rabbit.concurrency.metrics;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class RabbitQueueMetrics {

    private final MeterRegistry meterRegistry;
    private final ConnectionFactory connectionFactory;

    public RabbitQueueMetrics(MeterRegistry meterRegistry,
                              org.springframework.amqp.rabbit.connection.ConnectionFactory springConnectionFactory) {
        this.meterRegistry = meterRegistry;
        this.connectionFactory =
                ((org.springframework.amqp.rabbit.connection.CachingConnectionFactory)
                        springConnectionFactory).getRabbitConnectionFactory();
    }

    @PostConstruct
    public void registerMetrics() {

        Gauge.builder("rabbitmq.queue.message.count", this, m -> getMessageCount("stock.check.request"))
                .description("Queue icindeki message sayisi")
                .register(meterRegistry);

        Gauge.builder("rabbitmq.queue.consumer.count", this, m -> getConsumerCount("stock.check.request"))
                .description("Queue'ya bagli consumer sayisi")
                .register(meterRegistry);
    }

    private int getMessageCount(String queueName) {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            return channel.queueDeclarePassive(queueName).getMessageCount();
        } catch (Exception e) {
            return -1;
        }
    }

    private int getConsumerCount(String queueName) {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            return channel.queueDeclarePassive(queueName).getConsumerCount();
        } catch (Exception e) {
            return -1;
        }
    }
}
