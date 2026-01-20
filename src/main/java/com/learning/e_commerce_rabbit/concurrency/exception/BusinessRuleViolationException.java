package com.learning.e_commerce_rabbit.concurrency.exception;

/**
 * Business rule violation (stok yok, kart geçersiz)
 * → FATAL: DLQ'ya gönder
 */
public class BusinessRuleViolationException extends BusinessException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }

    @Override
    public boolean isFatal() {
        return true;
    }
}