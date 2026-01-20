package com.learning.e_commerce_rabbit.concurrency.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {

    private final Counter stockRejectedCounter;

    public OrderMetrics(MeterRegistry meterRegistry) {
        this.stockRejectedCounter = Counter.builder("order.stock.rejected")
                .description("Stock yetersizligi nedeniyle reddedilen order sayisi")
                .register(meterRegistry);
    }

    public void incrementStockRejected() {
        stockRejectedCounter.increment();
    }
}
