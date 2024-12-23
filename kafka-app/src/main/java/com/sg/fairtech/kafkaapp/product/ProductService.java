package com.sg.fairtech.kafkaapp.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sg.fairtech.kafkaapp.kafka.producer.ProductActivitiesProducer;
import com.sg.fairtech.kafkaapp.kafka.producer.ProductCheckDataProducer;
import com.sg.fairtech.kafkaapp.product.dto.ProductKafkaResponse;
import com.sg.fairtech.kafkaapp.product.dto.ProductRequest;
import com.sg.fairtech.kafkaapp.product.dto.ProductResponse;
import com.sg.fairtech.kafkaapp.product.helper.TransactionStateEnum;
import com.sg.fairtech.kafkaapp.product.model.ProductModel;
import com.sg.fairtech.kafkaapp.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductActivitiesProducer productActivitiesProducer;

    @Autowired
    private ProductCheckDataProducer productCheckDataProducer;

    public List<ProductResponse> getProductsWithValidSku() {
        return productRepository.findBySkuIsNotNull().stream().map(productModel -> new ProductResponse(
                productModel.getVersion(),
                productModel.getSku(),
                productModel.getName(),
                productModel.getDescription(),
                productModel.getPrice(),
                productModel.getConsumerProductIdentifier()
        )).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(productModel -> new ProductResponse(
                productModel.getVersion(),
                productModel.getSku(),
                productModel.getName(),
                productModel.getDescription(),
                productModel.getPrice(),
                productModel.getConsumerProductIdentifier()
        )).collect(Collectors.toList());
    }

    public ProductResponse getProductBySku(String sku) {
        Optional<ProductModel> productModelOptional = productRepository.findBySku(sku);

        // If productModel is null, return null or a default ProductResponse
        if (productModelOptional.isEmpty()) {
            return null; // Or return a default ProductResponse with empty values
        }

        ProductModel productModel = productModelOptional.get();

        return new ProductResponse(
                productModel.getVersion(),
                productModel.getSku(),
                productModel.getName(),
                productModel.getDescription(),
                productModel.getPrice(),
                productModel.getConsumerProductIdentifier()
        );
    }

    public ProductResponse saveProduct(ProductRequest productRequest) {
        ProductModel productModel = productRepository.save(productRequest);
        return new ProductResponse(
                productModel.getVersion(),
                productModel.getSku(),
                productModel.getName(),
                productModel.getDescription(),
                productModel.getPrice(),
                productModel.getConsumerProductIdentifier()
        );
    }

    public ProductResponse updateProduct(String sku, ProductRequest productRequest, Long version) throws Exception {
        ProductResponse productResponse = productRepository.findBySku(sku).map(productModel -> {
            if (version != null) productModel.setVersion(version);
            productModel.setName(productRequest.getName());
            productModel.setDescription(productRequest.getDescription());
            productModel.setPrice(productRequest.getPrice());
            productModel.setConsumerProductIdentifier(productRequest.getConsumerProductIdentifier());
            productRepository.save(productModel);
            return new ProductResponse(
                    productModel.getVersion(),
                    sku,
                    productModel.getName(),
                    productModel.getDescription(),
                    productModel.getPrice(),
                    productModel.getConsumerProductIdentifier()
            );
        }).orElseThrow(() -> new Exception("Failed to update data"));

        return productResponse;
    }

    public void deleteProduct(String sku) {
        productRepository.deleteBySku(sku);
    }

    public void publishProductToLastActivities(String sku, ProductResponse productResponse, TransactionStateEnum requestMethod) throws JsonProcessingException {
        ProductKafkaResponse productKafkaResponse = new ProductKafkaResponse();
        productKafkaResponse.setRequestMethod(requestMethod);
        productKafkaResponse.setSku(sku);
        if (requestMethod != TransactionStateEnum.DELETE || productResponse != null) {
            productKafkaResponse.setName(productResponse.getName());
            productKafkaResponse.setDescription(productResponse.getDescription());
            productKafkaResponse.setPrice(productResponse.getPrice());
        }
        String productKafkaResponseString = new ObjectMapper().writeValueAsString(productKafkaResponse);
        productActivitiesProducer.sendMessage(productKafkaResponseString);
    }

    public void publishProductToCheckData(String sku, ProductResponse productResponse) throws JsonProcessingException {
        ProductKafkaResponse productKafkaResponse = new ProductKafkaResponse();
        productKafkaResponse.setVersion(productResponse.getVersion());
        productKafkaResponse.setSku(sku);
        productKafkaResponse.setName(productResponse.getName());
        productKafkaResponse.setDescription(productResponse.getDescription());
        productKafkaResponse.setPrice(productResponse.getPrice());
        productKafkaResponse.setConsumerProductIdentifier(productResponse.getConsumerProductIdentifier());
        String productKafkaResponseString = new ObjectMapper().writeValueAsString(productKafkaResponse);
        productCheckDataProducer.sendMessage(productKafkaResponseString);
    }

    public static String generateSku(String productName) {
        String sanitizedProductName = productName.replaceAll("\\s+", "-").toUpperCase();
        String uniqueId = UUID.randomUUID().toString().substring(0, 8); // Shortened UUID
        return "SKU-" + sanitizedProductName + "-" + uniqueId;
    }

}
