package com.sg.fairtech.kafkaappconsumer1.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class ProductModel {


    public ProductModel(String sku, String name, String description, BigDecimal price) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public ProductModel(String sku, String name, String description, BigDecimal price, String consumerProductIdentifier) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.consumerProductIdentifier = consumerProductIdentifier;
    }

    public ProductModel(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String sku; // SKU: Unique identifier for the product

    @Version
    private Long version;

    private BigDecimal price;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String consumerProductIdentifier;

    @PrePersist
    public void prePersist() {
        // Generate the ID if not already set
        if (this.consumerProductIdentifier == null || this.consumerProductIdentifier.isEmpty()) {
            this.consumerProductIdentifier = "consumer-1-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();;
        }
    }

}
