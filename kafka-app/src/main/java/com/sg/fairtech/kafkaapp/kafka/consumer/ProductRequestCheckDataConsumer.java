package com.sg.fairtech.kafkaapp.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sg.fairtech.kafkaapp.kafka.producer.ProductCheckDataProducer;
import com.sg.fairtech.kafkaapp.product.ProductService;
import com.sg.fairtech.kafkaapp.product.dto.ProductKafkaRequest;
import com.sg.fairtech.kafkaapp.product.dto.ProductRequest;
import com.sg.fairtech.kafkaapp.product.dto.ProductResponse;
import com.sg.fairtech.kafkaapp.product.helper.ProductKafkaConverter;
import com.sg.fairtech.kafkaapp.product.helper.TransactionStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductRequestCheckDataConsumer {

    @Autowired
    ProductService productService;

    @Autowired
    ProductCheckDataProducer productCheckDataProducer;

    @KafkaListener(topics = "request_check_data", groupId = "group_product_request_check_data_1")
    public void consume(String productJsonString) throws Exception {
        ProductKafkaRequest productKafkaRequest = ProductKafkaConverter.convertStringToProductKafkaRequest(productJsonString);
        TransactionStateEnum requestMethod = productKafkaRequest.getRequestMethod();

        if (requestMethod == TransactionStateEnum.GET) {
            ProductResponse currentProduct = productService.getProductBySku(productKafkaRequest.getSku());
            ProductRequest productRequest = new ProductRequest(
                    productKafkaRequest.getName(),
                    productKafkaRequest.getDescription(),
                    productKafkaRequest.getPrice(),
                    productKafkaRequest.getConsumerProductIdentifier()
            );

            ProductResponse productResponse;
            if (currentProduct != null) {
                productRequest.setSku(productKafkaRequest.getSku());
                Long currentProductVersion = currentProduct.getVersion();
                Long productFromKafkaVersion = productKafkaRequest.getVersion();
                if (productFromKafkaVersion > currentProductVersion) currentProductVersion = productFromKafkaVersion;
                productResponse = productService.updateProduct(productRequest.getSku(), productRequest, currentProductVersion);
            } else {
                productRequest.setSku(ProductService.generateSku(productKafkaRequest.getName()));
                productResponse = productService.saveProduct(productRequest);
            }
            productService.publishProductToCheckData(productResponse.getSku(), productResponse);
        }
    }

}
