package com.learning.e_commerce_rabbit.concurrency.exception;

/**
 * External service timeout (banka API, stok servisi)
 * → TRANSIENT: Retry edilebilir
 */
public class ExternalServiceException extends BusinessException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean isFatal() {
        return false;  // Retry edilebilir
    }
}