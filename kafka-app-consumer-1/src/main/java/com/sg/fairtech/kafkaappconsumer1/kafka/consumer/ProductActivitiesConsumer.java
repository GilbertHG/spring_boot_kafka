package com.sg.fairtech.kafkaappconsumer1.kafka.consumer;

import com.sg.fairtech.kafkaappconsumer1.product.ProductService;
import com.sg.fairtech.kafkaappconsumer1.product.dto.ProductKafkaRequest;
import com.sg.fairtech.kafkaappconsumer1.product.dto.ProductRequest;
import com.sg.fairtech.kafkaappconsumer1.product.helper.ProductKafkaConverter;
import com.sg.fairtech.kafkaappconsumer1.product.helper.TransactionStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductActivitiesConsumer {

    @Autowired
    ProductService productService;

    @KafkaListener(topics = "get_last_activities", groupId = "group_last_activities_1")
    public void consume(String productJsonString) throws Exception {
        ProductKafkaRequest productKafkaRequest = ProductKafkaConverter.convertStringToProductKafkaRequest(productJsonString);
        TransactionStateEnum requestMethod = productKafkaRequest.getRequestMethod();
        String sku = productKafkaRequest.getSku();
        if (requestMethod == TransactionStateEnum.CREATE || requestMethod == TransactionStateEnum.UPDATE) {
            ProductRequest productRequest = new ProductRequest(
                    productKafkaRequest.getSku(),
                    productKafkaRequest.getName(),
                    productKafkaRequest.getDescription(),
                    productKafkaRequest.getPrice(),
                    productKafkaRequest.getConsumerProductIdentifier()
            );
            if (requestMethod == TransactionStateEnum.CREATE) {
                productService.saveProduct(productRequest);
            } else {
                productService.updateProduct(sku, productRequest, null);
            }
        } else if (requestMethod == TransactionStateEnum.DELETE) {
            productService.deleteProduct(sku);
        }
    }

}
