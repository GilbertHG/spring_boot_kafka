package com.sg.fairtech.kafkaappconsumer1.product.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sg.fairtech.kafkaappconsumer1.product.ProductService;
import com.sg.fairtech.kafkaappconsumer1.product.dto.ProductResponse;
import com.sg.fairtech.kafkaappconsumer1.product.helper.TransactionStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SyncProducts {

    @Autowired
    ProductService productService;

    public void syncProducts() {
        List<ProductResponse> test = productService.getAllProducts();
        productService.getAllProducts().forEach(productResponse -> {
            try {
                productService.publishProductToRequestCheckData(productResponse, TransactionStateEnum.GET);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Scheduled(cron = "0 1 0 * * *")
    public void runSyncProducts() {
        syncProducts();
    }

}
