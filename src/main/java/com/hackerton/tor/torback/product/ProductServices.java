package com.hackerton.tor.torback.product;

import com.hackerton.tor.torback.entity.Product;
import com.hackerton.tor.torback.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

@Slf4j
@AllArgsConstructor
@Service
public class ProductServices {
    ProductRepository productRepository;

    /**
     * [FIXME] Need to change Elasticsearch.
     * @param presetName
     * @return Flux<Product>
     */
    public Flux<Product> getProductListsByPresetName(@NotNull String presetName){
        return this.productRepository.getProductListByPresetName(presetName);
    }

    public Flux<Product> getSameCategoryProduct(@NotNull int productId){
        return this.productRepository.getSameCategoryProducts(productId);
    }

    public Flux<Product> getProductListsByCategoryName(@NotNull String categoryName){
        return this.productRepository.getProductListByCategoryName(categoryName)
                .doOnError(error -> log.trace(error.getMessage()));
    }
}
