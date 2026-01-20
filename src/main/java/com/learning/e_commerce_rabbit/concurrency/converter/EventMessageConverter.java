package com.learning.e_commerce_rabbit.concurrency.converter;

import tools.jackson.databind.ObjectMapper;
import com.learning.e_commerce_rabbit.concurrency.event.OrderCreatedEventV2;
import com.learning.e_commerce_rabbit.concurrency.event.PaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EventMessageConverter implements MessageConverter {

    private final ObjectMapper objectMapper;

    private static final Map<String, Class<?>> TYPE_MAPPING = new HashMap<>();

    static {
        TYPE_MAPPING.put("order.created.v2", OrderCreatedEventV2.class);
        TYPE_MAPPING.put("payment.requested.v1", PaymentRequestedEvent.class);
    }

    @Override
    public Message toMessage(Object payload, MessageProperties props)
            throws MessageConversionException {

        try {
            String eventType = (String) props.getHeaders().get("event-type");
            Integer version = (Integer) props.getHeaders().get("event-version");

            // Header yoksa otomatik ekle
            if (eventType == null || version == null) {
                if (payload instanceof OrderCreatedEventV2) {
                    props.setHeader("event-type", "order.created");
                    props.setHeader("event-version", 2);
                } else if (payload instanceof PaymentRequestedEvent) {
                    props.setHeader("event-type", "payment.requested");
                    props.setHeader("event-version", 1);
                } else {
                    throw new MessageConversionException(
                            "Unknown event type: " + payload.getClass().getName()
                    );
                }
            }

            byte[] body = objectMapper.writeValueAsBytes(payload);

            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            props.setContentEncoding("UTF-8");

            return new Message(body, props);

        } catch (Exception e) {
            throw new MessageConversionException("Failed to serialize message", e);
        }
    }

    @Override
    public Object fromMessage(Message message)
            throws MessageConversionException {

        try {
            MessageProperties props = message.getMessageProperties();

            String eventType = (String) props.getHeaders().get("event-type");
            Integer version = (Integer) props.getHeaders().get("event-version");

            if (eventType == null || version == null) {
                throw new MessageConversionException("Missing event headers");
            }

            String key = eventType + ".v" + version;
            Class<?> targetClass = TYPE_MAPPING.get(key);

            if (targetClass == null) {
                throw new MessageConversionException(
                        "No mapping found for event: " + key
                );
            }

            return objectMapper.readValue(message.getBody(), targetClass);

        } catch (Exception e) {
            throw new MessageConversionException("Failed to deserialize message", e);
        }
    }
}