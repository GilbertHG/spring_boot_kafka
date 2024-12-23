package com.sg.fairtech.kafkaappconsumer1.kafka.consumer;

import com.sg.fairtech.kafkaappconsumer1.product.ProductService;
import com.sg.fairtech.kafkaappconsumer1.product.dto.ProductKafkaRequest;
import com.sg.fairtech.kafkaappconsumer1.product.dto.ProductRequest;
import com.sg.fairtech.kafkaappconsumer1.product.dto.ProductResponse;
import com.sg.fairtech.kafkaappconsumer1.product.helper.ProductKafkaConverter;
import com.sg.fairtech.kafkaappconsumer1.product.helper.TransactionStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductCheckDataConsumer {

    @Autowired
    ProductService productService;

    @KafkaListener(topics = "check_data", groupId = "group_product_request_check_data_1")
    public void consume(String productJsonString) throws Exception {
        ProductKafkaRequest productKafkaRequest = ProductKafkaConverter.convertStringToProductKafkaRequest(productJsonString);

        ProductResponse currentProduct = productService.getProductByConsumerProductIdentfier(productKafkaRequest.getConsumerProductIdentifier());
        ProductRequest productRequest = new ProductRequest(
                productKafkaRequest.getSku(),
                productKafkaRequest.getName(),
                productKafkaRequest.getDescription(),
                productKafkaRequest.getPrice(),
                productKafkaRequest.getConsumerProductIdentifier()
        );
        if (currentProduct != null) {
            Long currentProductVersion = currentProduct.getVersion();
            Long productFromKafkaVersion = productKafkaRequest.getVersion();
            if (productFromKafkaVersion > currentProductVersion) currentProductVersion = productFromKafkaVersion;
            productService.updateProductConsumerProductIdentfier(productKafkaRequest.getConsumerProductIdentifier(), productRequest, currentProductVersion);
        } else {
            productService.saveProduct(productRequest);
        }
    }

}
