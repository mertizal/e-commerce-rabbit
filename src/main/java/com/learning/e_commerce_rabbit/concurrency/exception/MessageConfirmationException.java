package com.learning.e_commerce_rabbit.concurrency.exception;

/**
 * Mesajın RabbitMQ Broker tarafından onaylanmaması durumu (NACK)
 * → FATAL: Broker mesajın sorumluluğunu almadı, veri kaybı riski var.
 */
public class MessageConfirmationException extends BusinessException {

    public MessageConfirmationException(String message) {
        super(message);
    }

    public MessageConfirmationException(String message, String cause) {
        super(message + " | Reason: " + cause);
    }

    @Override
    public boolean isFatal() {
        // Broker onayı gelmemesi kritik bir sistem/altyapı hatasıdır.
        return true;
    }
}