package com.sg.fairtech.kafkaapp.product.assembler;

import com.sg.fairtech.kafkaapp.product.controller.ProductController;
import com.sg.fairtech.kafkaapp.product.dto.ProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
