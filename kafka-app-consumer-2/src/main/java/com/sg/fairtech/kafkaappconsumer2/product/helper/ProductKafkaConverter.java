package com.sg.fairtech.kafkaappconsumer2.product.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sg.fairtech.kafkaappconsumer2.product.dto.ProductKafkaRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductKafkaConverter {

    public static ProductKafkaRequest convertStringToProductKafkaRequest(String productJsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductKafkaRequest productKafkaRequest = null;
        try {
            productKafkaRequest = objectMapper.readValue(productJsonString, ProductKafkaRequest.class);
        } catch (Exception e) {
            log.error("An error occurred: " + e.getMessage());
        }
        return productKafkaRequest;
    }

}
