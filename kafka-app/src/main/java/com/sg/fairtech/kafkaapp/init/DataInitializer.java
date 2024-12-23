package com.sg.fairtech.kafkaapp.init;

import com.sg.fairtech.kafkaapp.product.model.ProductModel;
import com.sg.fairtech.kafkaapp.product.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer {

    @Autowired
    private ProductRepository productRepository;

    @PostConstruct
    public void init() {
        // Insert initial data if the table is empty
        if (productRepository.count() == 0) {
//            productRepository.save(new ProductModel(0L,"SKU001", "Product 1", "Description for product 1", BigDecimal.valueOf(19.99)));
//            productRepository.save(new ProductModel(0L,"SKU002", "Product 2", "Description for product 2", BigDecimal.valueOf(29.99)));
//            productRepository.save(new ProductModel(0L, "SKU003", "Product 3", "Description for product 3", BigDecimal.valueOf(39.99)));
        }
    }

}
