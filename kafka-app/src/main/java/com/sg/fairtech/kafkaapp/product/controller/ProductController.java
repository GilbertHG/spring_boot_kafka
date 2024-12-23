package com.sg.fairtech.kafkaapp.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sg.fairtech.kafkaapp.kafka.producer.ProductActivitiesProducer;
import com.sg.fairtech.kafkaapp.product.ProductService;
import com.sg.fairtech.kafkaapp.product.assembler.ProductAssembler;
import com.sg.fairtech.kafkaapp.product.dto.ProductRequest;
import com.sg.fairtech.kafkaapp.product.dto.ProductResponse;
import com.sg.fairtech.kafkaapp.product.helper.TransactionStateEnum;
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
    private ProductActivitiesProducer productActivitiesProducer;

    @Autowired
    private ProductAssembler assembler;

    @GetMapping
    public CollectionModel<EntityModel<ProductResponse>> index() {

        List<EntityModel<ProductResponse>> products = productService.getProductsWithValidSku()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(products, linkTo(methodOn(ProductController.class).index()).withSelfRel());
    }

    @GetMapping("/{sku}")
    public EntityModel<ProductResponse> getProduct(@PathVariable String sku) {
        ProductResponse productResponse = productService.getProductBySku(sku);

        return assembler.toModel(productResponse);
    }

    @Transactional
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductRequest productRequest) throws JsonProcessingException {
        ProductResponse productResponse = productService.saveProduct(productRequest);
        EntityModel<ProductResponse> productResponseEntityModel = assembler.toModel(productResponse);
        productService.publishProductToLastActivities(productResponse.getSku(), productResponse, TransactionStateEnum.CREATE);

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
        productService.publishProductToLastActivities(productResponse.getSku(), productResponse, TransactionStateEnum.UPDATE);

        return ResponseEntity
                .created(productResponseEntityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(productResponseEntityModel);
    }

    @Transactional
    @DeleteMapping("{sku}")
    public ResponseEntity<?> deleteProduct(@PathVariable String sku) throws JsonProcessingException {
        productService.deleteProduct(sku);
        productService.publishProductToLastActivities(sku, null, TransactionStateEnum.DELETE);

        return ResponseEntity.noContent().build();
    }

}
