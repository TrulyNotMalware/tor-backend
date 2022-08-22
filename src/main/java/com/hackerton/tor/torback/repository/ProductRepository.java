package com.hackerton.tor.torback.repository;

import com.hackerton.tor.torback.entity.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends R2dbcRepository<Product, String> {

    @Query("SELECT p.* FROM preset ps\n" +
            "JOIN preset_detail pd ON ps.presetId = pd.presetId\n" +
            "JOIN product p ON pd.productId=p.productId\n" +
            "WHERE ps.presetName=?")
    Flux<Product> getProductListByPresetName(String presetName);

    @Query("SELECT * FROM product\n" +
            "WHERE categoryName\n" +
            "IN(\n" +
            "    SELECT categoryName FROM product\n" +
            "    WHERE productId=:productId\n" +
            "          )\n" +
            "AND productId != :productId")
    Flux<Product> getSameCategoryProducts(@Param(value = "productId") int productId);

    @Query("SELECT * FROM product WHERE productId = :productId")
    Mono<Product> getProductById(@Param(value = "productId") int productId);

    @Query("SELECT * FROM product WHERE categoryName = ?")
    Flux<Product> getProductListByCategoryName(String categoryName);
}
