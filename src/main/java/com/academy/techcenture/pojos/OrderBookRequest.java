package com.academy.techcenture.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderBookRequest {
    private String customerName;
    private int bookId;
}