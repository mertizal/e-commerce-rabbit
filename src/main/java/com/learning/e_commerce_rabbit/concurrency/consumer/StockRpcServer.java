package com.learning.e_commerce_rabbit.concurrency.consumer;

import com.learning.e_commerce_rabbit.concurrency.config.StockRpcRabbitConfig;
import com.learning.e_commerce_rabbit.concurrency.event.StockCheckRequest;
import com.learning.e_commerce_rabbit.concurrency.event.StockCheckResponse;
import com.learning.e_commerce_rabbit.concurrency.service.MockStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class StockRpcServer {

    private final MockStockService mockStockService;

    @RabbitListener(
            queues = StockRpcRabbitConfig.STOCK_CHECK_REQUEST_QUEUE,
            containerFactory = "stockRpcListenerFactory"
    )
    public StockCheckResponse handleStockCheck(StockCheckRequest request) {

        log.info("STOCK RPC REQUEST | orderId={}", request.getOrderId());

        boolean hasStock = mockStockService.hasStock(request.getOrderId());

        StockCheckResponse response = new StockCheckResponse(
                request.getOrderId(),
                hasStock
        );

        log.info("STOCK RPC RESPONSE | orderId={} | available={}",
                request.getOrderId(), hasStock);

        return response; // Spring otomatik reply-to'ya gönderir
    }
}


