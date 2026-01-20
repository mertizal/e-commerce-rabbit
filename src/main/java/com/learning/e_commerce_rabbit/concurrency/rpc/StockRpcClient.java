package com.learning.e_commerce_rabbit.concurrency.rpc;

import com.learning.e_commerce_rabbit.concurrency.event.StockCheckRequest;
import com.learning.e_commerce_rabbit.concurrency.event.StockCheckResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockRpcClient {

    @Qualifier("stockRpcTemplate")
    private final RabbitTemplate stockRpcTemplate;

    public boolean checkStock(String orderId) {
        log.info("RPC → Sending stock check request | orderId={}", orderId);

        StockCheckRequest request = new StockCheckRequest(orderId);

        StockCheckResponse response = (StockCheckResponse) stockRpcTemplate
                .convertSendAndReceive(request);

        if (response == null) {
            log.error("RPC TIMEOUT | orderId={}", orderId);
            throw new IllegalStateException("Stock service timeout");
        }

        log.info("RPC ← Stock response received | orderId={} | available={}",
                orderId, response.isAvailable());

        return response.isAvailable();
    }
}
