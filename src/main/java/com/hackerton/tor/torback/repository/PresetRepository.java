package com.hackerton.tor.torback.repository;

import com.hackerton.tor.torback.entity.Preset;
import com.hackerton.tor.torback.entity.Preset_detail;
import com.hackerton.tor.torback.entity.Purchase_history;
import com.hackerton.tor.torback.entity.User_preset_binding;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PresetRepository extends R2dbcRepository<Preset, String> {

    @Query("SELECT * FROM preset ORDER BY recommend DESC LIMIT 10")
    Flux<Preset> getPresetListByRecommend();

    @Query("SELECT * FROM preset p JOIN user u\n" +
            "WHERE u.userId = p.producer\n" +
            "AND u.userId = ?")
    Flux<Preset> getMyPresetLists(String userId);

    @Query("SELECT * FROM preset WHERE presetId = ?")
    Mono<Preset> getPresetById(int presetId);

    @Query("SELECT * FROM preset WHERE presetName = ?")
    Mono<Preset> getPresetByName(String presetName);

    @Query("UPDATE preset\n" +
            "SET recommend = recommend + 1 \n" +
            "WHERE presetId =:presetId;\n" +
            "INSERT INTO user_preset_binding(userId, presetId, recommend)\n" +
            "VALUES (:userId,:presetId,1);" +
            "SELECT * FROM preset WHERE presetId = :presetId")
    Mono<Preset> updatePresetRecommend(@Param(value = "presetId") int presetId,@Param(value = "userId") String userId);

    @Query("INSERT INTO preset(presetName, presetContent, categoryName, producer)\n" +
            "VALUES(:presetName,:presetContent,:presetCategoryName,:producer);" +
            "SELECT * FROM preset WHERE presetName = :presetName")
    Mono<Preset> insertNewPreset(
            @Param(value = "presetName") String presetName,
            @Param(value = "presetContent") String presetContent,
            @Param(value = "presetCategoryName") String presetCategoryName,
            @Param(value = "producer") String producer);

    @Query("INSERT INTO preset_detail(presetId, categoryName, productId) VALUES (:presetId,:productCategoryName, :productId);\n" +
            "SELECT * FROM preset_detail WHERE presetId = :presetId AND productId = :productId;")
    Mono<Preset_detail> insertPresetDetail(
            @Param(value = "presetId") long presetId,
            @Param(value = "productCategoryName") String productCategoryName,
            @Param(value = "productId") long productId
    );

    @Query("UPDATE preset \n" +
            "SET buyCount = buyCount + 1 \n" +
            "WHERE presetId = :presetId;\n" +
            "SELECT * FROM preset WHERE presetId = :presetId;")
    Mono<Preset> updateBuyCount(@Param(value = "presetId") long presetId);

    @Query("INSERT INTO user_preset_binding(userId,presetId,buyCount) \n" +
            "VALUES (:userId,:presetId,:buyCount);" +
            "SELECT 1")
    Mono<User_preset_binding> insertNewUserPresetBinding(
            @Param(value = "userId") String userId,
            @Param(value = "presetId") long presetId,
            @Param(value = "buyCount") long buyCount
    );

    @Query("SELECT * FROM user_preset_binding\n" +
            "WHERE userId = :userId\n" +
            "AND buyCount > 0\n" +
            "ORDER BY createdAt DESC;")
    Flux<User_preset_binding> getPresetPurchasedHistory(
            @Param(value = "userId") String userId
    );
}
