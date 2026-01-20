package com.learning.e_commerce_rabbit.concurrency.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ConsumerMetrics {

    private final Counter orderCreatedConsumed;
    private final Counter orderCreatedSuccess;
    private final Counter orderCreatedFailed;
    private final Counter orderCreatedDuplicateIgnored;


    public ConsumerMetrics(MeterRegistry meterRegistry) {

        this.orderCreatedConsumed = Counter.builder("order.created.consumed")
                .description("OrderCreated queue'dan alinan mesaj sayisi")
                .register(meterRegistry);

        this.orderCreatedSuccess = Counter.builder("order.created.success")
                .description("Basariyla payment'a giden order sayisi")
                .register(meterRegistry);

        this.orderCreatedFailed = Counter.builder("order.created.failed")
                .description("Exception veya DLQ'ya giden order sayisi")
                .register(meterRegistry);

        this.orderCreatedDuplicateIgnored = Counter.builder("order.created.duplicate.failed")
                .description("Duplitacted olarak işlenmeye çalışlan order sayisi")
                .register(meterRegistry);
    }

    public void incrementOrderCreatedConsumed() {
        orderCreatedConsumed.increment();
    }

    public void incrementOrderCreatedSuccess() {
        orderCreatedSuccess.increment();
    }

    public void incrementOrderCreatedFailed() {
        orderCreatedFailed.increment();
    }

    public void incrementDuplicateIgnored() {

        orderCreatedDuplicateIgnored.increment();
    }
}

