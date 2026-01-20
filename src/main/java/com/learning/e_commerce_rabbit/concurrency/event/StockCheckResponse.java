package com.learning.e_commerce_rabbit.concurrency.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockCheckResponse {

    private String orderId;
    private boolean available;
}
