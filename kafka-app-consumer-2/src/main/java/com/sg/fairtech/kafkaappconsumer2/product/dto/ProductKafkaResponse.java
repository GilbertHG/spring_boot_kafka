package com.sg.fairtech.kafkaappconsumer2.product.dto;

import com.sg.fairtech.kafkaappconsumer2.product.helper.TransactionStateEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductKafkaResponse {

    private Long version;
    private TransactionStateEnum requestMethod;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String consumerProductIdentifier;

}
