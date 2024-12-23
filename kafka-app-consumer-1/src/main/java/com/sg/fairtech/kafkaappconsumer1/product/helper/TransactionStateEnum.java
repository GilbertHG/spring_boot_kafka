package com.sg.fairtech.kafkaappconsumer1.product.helper;

import lombok.Getter;

@Getter
public enum TransactionStateEnum {

    GET("get"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");

    private final String value;

    TransactionStateEnum(String value) {
        this.value = value;
    }
}
