package com.learning.e_commerce_rabbit.concurrency.config;

import com.learning.e_commerce_rabbit.concurrency.converter.EventMessageConverter;
import com.learning.e_commerce_rabbit.concurrency.exception.MessageConfirmationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitProducerConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            EventMessageConverter eventMessageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        template.setMessageConverter(eventMessageConverter);
        template.setMandatory(true);

        // CONFIRM CALLBACK
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info(
                        "MESSAGE CONFIRMED | correlationId={}",
                        correlationData != null ? correlationData.getId() : "null"
                );
            } else {
                log.error(
                        "MESSAGE NACKED | correlationId={} | cause={}",
                        correlationData != null ? correlationData.getId() : "null",
                        cause
                );
                throw new MessageConfirmationException(
                        "Message not confirmed by broker: " + cause
                );
            }
        });

        // RETURN CALLBACK (routing fail)
        template.setReturnsCallback(returned -> {
            log.error(
                    "MESSAGE RETURNED | exchange={} | routing={} | reply={} | message={}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyText(),
                    new String(returned.getMessage().getBody())
            );
        });

        return template;
    }
}
