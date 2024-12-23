package com.sg.fairtech.kafkaappconsumer2.product.assembler;

import com.sg.fairtech.kafkaappconsumer2.product.controller.ProductController;
import com.sg.fairtech.kafkaappconsumer2.product.dto.ProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ProductAssembler implements RepresentationModelAssembler<ProductResponse, EntityModel<ProductResponse>> {

    /**
     * Converts the given entity into a {@code D}, which extends {@link RepresentationModel}.
     *
     * @param entity
     * @return
     */
    @Override
    public EntityModel<ProductResponse> toModel(ProductResponse productResponse) {
        return EntityModel.of(
                productResponse,
                linkTo(methodOn(ProductController.class).getProduct(productResponse.getSku())).withSelfRel(),
                linkTo(methodOn(ProductController.class).index()).withRel("product")
            );
    }

}
