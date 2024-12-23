package com.sg.fairtech.kafkaappconsumer1.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    public ProductResponse(String sku, String name, String description, BigDecimal price, String consumerProductIdentifier) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.consumerProductIdentifier = consumerProductIdentifier;
    }

    public ProductResponse(String sku, String name, String description, BigDecimal price) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    private Long version;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String consumerProductIdentifier;

}
