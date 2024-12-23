package com.sg.fairtech.kafkaapp.product.repository;

import com.sg.fairtech.kafkaapp.product.dto.ProductRequest;
import com.sg.fairtech.kafkaapp.product.model.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel, Long> {

    Optional<ProductModel> findBySku(String sku);

    List<ProductModel> findBySkuIsNotNull();

    default ProductModel save(ProductRequest productRequest) {
        ProductModel productModel = this.save(new ProductModel(
                productRequest.getSku(),
                productRequest.getName(),
                productRequest.getDescription(),
                productRequest.getPrice(),
                productRequest.getConsumerProductIdentifier()
        ));

        return productModel;
    }

    void deleteBySku(String sku);

}
