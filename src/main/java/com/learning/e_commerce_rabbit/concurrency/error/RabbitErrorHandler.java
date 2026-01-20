package com.learning.e_commerce_rabbit.concurrency.error;

import com.learning.e_commerce_rabbit.concurrency.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

@Slf4j
@Component("rabbitMQErrorHandler")
public class RabbitErrorHandler implements ErrorHandler {

    @Override
    public void handleError(Throwable throwable) {

        // ListenerExecutionFailedException içindeki gerçek exception'ı al
        Throwable cause = throwable;
        if (throwable instanceof ListenerExecutionFailedException) {
            cause = throwable.getCause() != null ? throwable.getCause() : throwable;
        }

        // BusinessException kontrolü
        if (cause instanceof BusinessException businessException) {

            if (businessException.isFatal()) {
                // FATAL ERROR → NACK + DLQ
                log.error(
                        "FATAL ERROR | type={} | error={} | action=REJECT+DLQ",
                        businessException.getClass().getSimpleName(),
                        businessException.getMessage()
                );

                // AmqpRejectAndDontRequeueException → Spring NACK yapar (requeue=false)
                throw new AmqpRejectAndDontRequeueException(
                        "Fatal error - sent to DLQ: " + businessException.getMessage(),
                        businessException
                );

            } else {
                // TRANSIENT ERROR → Consumer retry yapacak
                log.warn(
                        "TRANSIENT ERROR | type={} | error={} | action=Consumer will retry",
                        businessException.getClass().getSimpleName(),
                        businessException.getMessage()
                );

                // Transient error'ı tekrar fırlat, consumer yakalayıp retry yapacak
                throw businessException;
            }
        }

        // UNKNOWN ERROR → FATAL kabul et
        log.error(
                "UNKNOWN ERROR | type={} | error={} | action=REJECT+DLQ",
                cause.getClass().getSimpleName(),
                cause.getMessage(),
                cause
        );

        throw new AmqpRejectAndDontRequeueException(
                "Unknown error - sent to DLQ",
                cause
        );
    }
}