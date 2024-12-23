package com.sg.fairtech.kafkaappconsumer2.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    public ProductRequest(String name, String description, BigDecimal price, String consumerProductIdentifier) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.consumerProductIdentifier = consumerProductIdentifier;
    }

    public ProductRequest(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String consumerProductIdentifier;

}
