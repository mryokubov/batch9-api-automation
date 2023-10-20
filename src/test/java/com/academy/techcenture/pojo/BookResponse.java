package com.academy.techcenture.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class BookResponse {

    private int id;
    private String name;
    private String author;
    private String isbn;
    private String type;
    private double price;
    @JsonProperty("current-stock")
    private int currentStock;
    private boolean available;


}
