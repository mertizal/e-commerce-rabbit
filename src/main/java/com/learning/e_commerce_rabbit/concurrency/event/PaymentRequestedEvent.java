package com.learning.e_commerce_rabbit.concurrency.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestedEvent {

    private String eventId;
    String orderId;
    double amount;
}
