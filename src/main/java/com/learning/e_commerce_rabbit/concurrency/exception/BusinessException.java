package com.learning.e_commerce_rabbit.concurrency.exception;

/**
 * Base exception for all business-related errors
 */
public abstract class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Fatal = retry edilmez, DLQ'ya gider
     * Transient = retry edilir
     */
    public abstract boolean isFatal();
}