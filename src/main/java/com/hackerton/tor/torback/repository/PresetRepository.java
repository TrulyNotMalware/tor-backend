package com.hackerton.tor.torback.repository;

import com.hackerton.tor.torback.entity.Preset;
import com.hackerton.tor.torback.entity.Preset_detail;
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
            "WHERE presetId =:presetId; \n" +
            "SELECT * FROM preset WHERE presetId = :presetId")
    Mono<Preset> updatePresetRecommend(@Param(value = "presetId") int presetId);

    @Query("INSERT INTO preset(presetName, presetContent, categoryName, producer)\n" +
            "VALUES(:presetName,:presetContent,:presetCategoryName,:producer);" +
            "SELECT * FROM preset WHERE presetName = :presetName")
    Mono<Preset> insertNewPreset(
            @Param(value = "presetName") String presetName,
            @Param(value = "presetContent") String presetContent,
            @Param(value = "presetCategoryName") String presetCategoryName,
            @Param(value = "producer") String producer);

    @Query("INSERT INTO preset_detail(presetId, categoryName, productId) VALUES (:presetId,:'productCategoryName', :productId);\n" +
            "SELECT * FROM preset_detail WHERE presetId = :presetId AND productId = :productId;")
    Mono<Preset_detail> insertPresetDetail(
            @Param(value = "presetId") long presetId,
            @Param(value = "productCategoryName") String productCategoryName,
            @Param(value = "productId") long productId
    );
}
