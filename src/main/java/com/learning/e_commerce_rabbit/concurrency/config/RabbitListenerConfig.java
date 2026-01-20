package com.learning.e_commerce_rabbit.concurrency.config;

import com.learning.e_commerce_rabbit.concurrency.converter.EventMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

@Slf4j
@Configuration
public class RabbitListenerConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
//            JacksonJsonMessageConverter jacksonJsonMessageConverter,
            EventMessageConverter eventMessageConverter,
            @Qualifier("rabbitMQErrorHandler") ErrorHandler errorHandler  //Inject
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(eventMessageConverter);

        // AUTO mode - Spring exception'a göre ACK/NACK yapar
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(2);
        factory.setObservationEnabled(true);
        factory.setPrefetchCount(1);

        // Exception olursa requeue YAPMA (DLQ'ya gitsin)
        factory.setDefaultRequeueRejected(false);

        // MERKEZI ERROR HANDLER (Component'ten inject edildi)
        factory.setErrorHandler(errorHandler);

        return factory;
    }
}