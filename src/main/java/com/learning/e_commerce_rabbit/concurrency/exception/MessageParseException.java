package com.learning.e_commerce_rabbit.concurrency.exception;

/**
 * Message parse edilemedi (JSON hatalı)
 * → FATAL: DLQ'ya gönder
 */
public class MessageParseException extends BusinessException {

    public MessageParseException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean isFatal() {
        return true;
    }
}