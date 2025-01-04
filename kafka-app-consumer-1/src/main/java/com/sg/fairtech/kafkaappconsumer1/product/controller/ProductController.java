package com.sg.fairtech.kafkaappconsumer1.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sg.fairtech.kafkaappconsumer1.product.ProductService;
import com.sg.fairtech.kafkaappconsumer1.product.assembler.ProductAssembler;
import com.sg.fairtech.kafkaappconsumer1.product.dto.ProductRequest;
import com.sg.fairtech.kafkaappconsumer1.product.dto.ProductResponse;
import com.sg.fairtech.kafkaappconsumer1.product.helper.TransactionStateEnum;
import com.sg.fairtech.kafkaappconsumer1.product.schedule.SyncProducts;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductAssembler assembler;

    @Autowired
    private SyncProducts syncProducts;

    @GetMapping
    public CollectionModel<EntityModel<ProductResponse>> index() {

        List<ProductResponse> productResponses = productService.getProductsWithValidSku();

        // List to collect all CompletableFuture instances
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        productResponses.forEach(productResponse -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    productService.publishProductToRequestCheckData(productResponse, TransactionStateEnum.GET);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        });

        // Wait for all futures to complete
        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allDone.get(); // Wait for completion
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error waiting for Kafka processing to complete", e);
        }

        List<ProductResponse> productResponsesLatest = productService.getProductsWithValidSku();
        List<EntityModel<ProductResponse>> productsLatest = productResponsesLatest
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(productsLatest, linkTo(methodOn(ProductController.class).index()).withSelfRel());
    }

    @GetMapping("/{sku}")
    public EntityModel<ProductResponse> getProduct(@PathVariable String sku) {
        ProductResponse productResponse = productService.getProductBySku(sku);

        // Fire-and-forget Kafka publish
        CompletableFuture.runAsync(() -> {
            try {
                productService.publishProductToRequestCheckData(productResponse, TransactionStateEnum.GET);
            } catch (JsonProcessingException e) {
                // Log the error instead of throwing an exception
                log.error("Error publishing to Kafka: " + e.getMessage());
            }
        });

        ProductResponse productResponseLatest = productService.getProductBySku(sku);

        return assembler.toModel(productResponseLatest);
    }

    @Transactional
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductRequest productRequest) {
        ProductResponse productResponse = productService.saveProduct(productRequest);
        EntityModel<ProductResponse> productResponseEntityModel = assembler.toModel(productResponse);

        return ResponseEntity
                .created(productResponseEntityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(productResponseEntityModel);
    }

    @SneakyThrows
    @Transactional
    @PatchMapping("/{sku}")
    public ResponseEntity<?> updateProduct(@PathVariable String sku, @RequestBody ProductRequest productRequest) {
        ProductResponse productResponse = productService.updateProduct(sku, productRequest, null);
        EntityModel<ProductResponse> productResponseEntityModel = assembler.toModel(productResponse);

        return ResponseEntity
                .created(productResponseEntityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(productResponseEntityModel);
    }

    @Transactional
    @DeleteMapping("{sku}")
    public ResponseEntity<?> deleteProduct(@PathVariable String sku) {
        productService.deleteProduct(sku);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sync-products")
    public String triggerSyncProducts() {
        syncProducts.runSyncProducts();
        return "Sync Products Job has Executed";
    }

}
