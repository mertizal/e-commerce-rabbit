package com.learning.e_commerce_rabbit.concurrency.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEventV2 {

    private String eventId;
    private String orderId;
    private double price;
    private boolean premium;
    private String campaignCode;


}
