package com.hackerton.tor.torback.product;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@Slf4j
@AllArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/product")
public class ProductController {

    private ProductServices services;

    @GetMapping(value = "/getProductList/{presetName}", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<?>> getProductListsByPresetName(
            @PathVariable String presetName
    ){
        /**
         * Get Product list by presetName.
         * Required : presetName ( String )
         * Return List<Product>
         */
        Mono<Links> allLinks;
        Mono<Link> selfLink = linkTo(methodOn(ProductController.class).getProductListsByPresetName(presetName))
                .withSelfRel().toMono();

        return Mono.zip(this.services.getProductListsByPresetName(presetName)
                        .log("getProductListBy")
                .collectList()
                        .log("CollectList"),selfLink)
                .map(objects -> CollectionModel.of(objects.getT1(),objects.getT2()));
    }

    @GetMapping(value = "/getSameCategoryProducts/{productId}",produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<?>> getSameProduct(@PathVariable int productId){
        /**
         * Get Same Category Product lists.
         * Required : productId ( int )
         * Return List<Product>
         */
        Mono<Links> allLinks;
        Mono<Link> selfLink = linkTo(methodOn(ProductController.class).getSameProduct(productId))
                .withSelfRel().toMono();
        return Mono.zip(this.services.getSameCategoryProduct(productId).collectList(),selfLink)
                .map(objects -> CollectionModel.of(objects.getT1(),objects.getT2()));
    }

    @GetMapping(value = "/getProductLists/{categoryName}", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<?>> getProductListsByCategoryName( @PathVariable String categoryName){
        Mono<Link> selfLink = linkTo(methodOn(ProductController.class).getProductListsByCategoryName(categoryName))
                .withSelfRel().toMono();

        return Mono.zip(this.services.getProductListsByCategoryName(categoryName).collectList(),selfLink)
                .map(objects -> CollectionModel.of(objects.getT1(),objects.getT2()));
    }
//    @GetMapping(value = "/getProductImage/{productId}",produces = MediaType.IMAGE_JPEG_VALUE)
//    public Mono<ServerResponse> getProductImages(@PathVariable String productId){
//
//    }
}
