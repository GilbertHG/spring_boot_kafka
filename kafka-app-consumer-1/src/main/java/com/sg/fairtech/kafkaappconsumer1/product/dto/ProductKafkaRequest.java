package com.sg.fairtech.kafkaappconsumer1.product.dto;

import com.sg.fairtech.kafkaappconsumer1.product.helper.TransactionStateEnum;
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
public class ProductKafkaRequest {

    private Long version;
    private TransactionStateEnum requestMethod;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String consumerProductIdentifier;

}
