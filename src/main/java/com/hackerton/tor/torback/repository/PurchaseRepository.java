package com.hackerton.tor.torback.repository;

import com.hackerton.tor.torback.entity.Purchase_history;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

public interface PurchaseRepository extends R2dbcRepository<Purchase_history, String> {

    @Query("INSERT INTO purchase_history(userId,productId,count) \n" +
            "VALUES(:userId,:productId,:count);" +
            "SELECT * FROM purchase_history WHERE userId=:userId AND productId=:productId " +
            "ORDER BY createdAt DESC LIMIT 1")
    Mono<Purchase_history> insertNewPurchaseHistory(
            @Param(value = "userId") String userId,
            @Param(value = "productId") String productId,
            @Param(value = "count") int count
    );
}
