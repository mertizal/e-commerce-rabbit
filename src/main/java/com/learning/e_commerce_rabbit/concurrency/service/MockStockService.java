package com.learning.e_commerce_rabbit.concurrency.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class MockStockService {

    private final Random random = new Random();

    public boolean hasStock(String orderId) {
        boolean available = random.nextBoolean();

        log.info("Stock check | orderId={} | available={}", orderId, available);

        return available;
    }

}
