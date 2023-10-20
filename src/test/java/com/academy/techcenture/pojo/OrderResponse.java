package com.academy.techcenture.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderResponse {

    private String id;
    private int bookId;
    private String customerName;
    private String createdBy;
    private int quantity;
    private long timestamp;
}
