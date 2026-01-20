package com.learning.e_commerce_rabbit.concurrency.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String id;
    private String product;
    private double price;

    private OrderStatus status;
    private boolean premium;
    private String campaignCode;


    public enum OrderStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}



