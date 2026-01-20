package com.learning.e_commerce_rabbit.concurrency.controllers;

import com.learning.e_commerce_rabbit.concurrency.domain.Order;
import com.learning.e_commerce_rabbit.concurrency.producer.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderEventProducer orderEventProducer;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody Order order) {
        order.setStatus(Order.OrderStatus.PENDING);
        order.setId(UUID.randomUUID().toString());
        orderEventProducer.publishOrderCreated(order);
        return ResponseEntity.ok("Order accepted");
    }

//    @PostMapping
//    public ResponseEntity<String> createOrder(@RequestBody Order order) {
//        order.setStatus(Order.OrderStatus.PENDING);
//        order.setId("dbdbf3ea-d468-437c-95a1-6654b0899d67"); // Test için sabit ID
//        orderEventProducer.publishOrderCreated(order);
//        return ResponseEntity.ok("Order accepted");
//    }

}

