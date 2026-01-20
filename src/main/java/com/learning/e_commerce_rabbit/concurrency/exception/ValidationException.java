package com.learning.e_commerce_rabbit.concurrency.exception;

/**
 * Validation hatası (price negatif, required field yok)
 * → FATAL: DLQ'ya gönder
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(message);
    }

    @Override
    public boolean isFatal() {
        return true;
    }
}