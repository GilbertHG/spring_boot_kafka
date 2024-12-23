package com.sg.fairtech.kafkaappconsumer2.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sg.fairtech.kafkaappconsumer2.kafka.producer.ProductRequestCheckDataProducer;
import com.sg.fairtech.kafkaappconsumer2.product.dto.ProductKafkaResponse;
import com.sg.fairtech.kafkaappconsumer2.product.dto.ProductRequest;
import com.sg.fairtech.kafkaappconsumer2.product.dto.ProductResponse;
import com.sg.fairtech.kafkaappconsumer2.product.helper.TransactionStateEnum;
import com.sg.fairtech.kafkaappconsumer2.product.model.ProductModel;
import com.sg.fairtech.kafkaappconsumer2.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductRequestCheckDataProducer productRequestCheckDataProducer;

    public List<ProductResponse> getProductsWithValidSku() {
        return productRepository.findBySkuIsNotNull().stream().map(productModel -> new ProductResponse(
                productModel.getSku(),
                productModel.getName(),
                productModel.getDescription(),
                productModel.getPrice(),
                productModel.getConsumerProductIdentifier()
        )).collect(Collectors.toList());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(productModel -> new ProductResponse(
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
                productModel.getSku(),
                productModel.getName(),
                productModel.getDescription(),
                productModel.getPrice(),
                productModel.getConsumerProductIdentifier()
        );
    }

    public ProductResponse getProductByConsumerProductIdentfier(String consumerProductIdentifier) {
        Optional<ProductModel> productModelOptional = productRepository.findByConsumerProductIdentifier(consumerProductIdentifier);

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
            productModel.setSku(sku);
            productModel.setName(productRequest.getName());
            productModel.setDescription(productRequest.getDescription());
            productModel.setPrice(productRequest.getPrice());
            productRepository.save(productModel);
            return new ProductResponse(
                    sku,
                    productModel.getName(),
                    productModel.getDescription(),
                    productModel.getPrice(),
                    productModel.getConsumerProductIdentifier()
            );
        }).orElseThrow(() -> new Exception("Failed to update data"));

        return productResponse;
    }

    public ProductResponse updateProductConsumerProductIdentfier(String consumerProductIdentifier, ProductRequest productRequest, Long version) throws Exception {
        ProductResponse productResponse = productRepository.findByConsumerProductIdentifier(consumerProductIdentifier).map(productModel -> {
            if (version != null) productModel.setVersion(version);
            productModel.setSku(productRequest.getSku());
            productModel.setName(productRequest.getName());
            productModel.setDescription(productRequest.getDescription());
            productModel.setPrice(productRequest.getPrice());
            productModel.setConsumerProductIdentifier(productRequest.getConsumerProductIdentifier());
            productRepository.save(productModel);
            return new ProductResponse(
                    productRequest.getSku(),
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

    public void publishProductToRequestCheckData(ProductResponse productResponse, TransactionStateEnum requestMethod) throws JsonProcessingException {
        ProductKafkaResponse productKafkaResponse = new ProductKafkaResponse();
        productKafkaResponse.setRequestMethod(requestMethod);
        productKafkaResponse.setName(productResponse.getName());
        productKafkaResponse.setDescription(productResponse.getDescription());
        productKafkaResponse.setPrice(productResponse.getPrice());
        productKafkaResponse.setConsumerProductIdentifier(productResponse.getConsumerProductIdentifier());
        String productKafkaResponseString = new ObjectMapper().writeValueAsString(productKafkaResponse);
        productRequestCheckDataProducer.sendMessage(productKafkaResponseString);
    }

}
