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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
        List<EntityModel<ProductResponse>> products = productResponses
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        productResponses.forEach( productResponse -> {
            try {
                productService.publishProductToRequestCheckData(productResponse, TransactionStateEnum.GET);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        return CollectionModel.of(products, linkTo(methodOn(ProductController.class).index()).withSelfRel());
    }

    @GetMapping("/{sku}")
    public EntityModel<ProductResponse> getProduct(@PathVariable String sku) {
        ProductResponse productResponse = productService.getProductBySku(sku);
        try {
            productService.publishProductToRequestCheckData(productResponse, TransactionStateEnum.GET);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return assembler.toModel(productResponse);
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
