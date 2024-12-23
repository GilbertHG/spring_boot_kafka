package com.sg.fairtech.kafkaappconsumer2.product.repository;

import com.sg.fairtech.kafkaappconsumer2.product.dto.ProductRequest;
import com.sg.fairtech.kafkaappconsumer2.product.model.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel, Long> {

    Optional<ProductModel> findBySku(String sku);

    Optional<ProductModel> findByConsumerProductIdentifier(String consumerProductIdentifier);

    List<ProductModel> findBySkuIsNotNull();

    default ProductModel save(ProductRequest productRequest) {
        return this.save(new ProductModel(
                productRequest.getSku(),
                productRequest.getName(),
                productRequest.getDescription(),
                productRequest.getPrice(),
                productRequest.getConsumerProductIdentifier()
        ));
    }

    void deleteBySku(String sku);

}
