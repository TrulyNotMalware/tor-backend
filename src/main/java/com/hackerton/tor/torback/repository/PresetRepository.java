package com.hackerton.tor.torback.repository;

import com.hackerton.tor.torback.entity.Preset;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PresetRepository extends R2dbcRepository<Preset, String> {

    @Query("SELECT * FROM preset ORDER BY recommend DESC LIMIT 20")
    Flux<Preset> getPresetListByRecommend();

    @Query("SELECT * FROM preset p JOIN user u\n" +
            "WHERE u.userId = p.producer\n" +
            "AND u.userId = ?")
    Flux<Preset> getMyPresetLists(String userId);

    @Query("SELECT * FROM preset WHERE presetId = ?")
    Mono<Preset> getPresetById(int presetId);

    @Query("SELECT * FROM preset WHERE presetName = ?")
    Mono<Preset> getPresetByName(String presetName);
}
