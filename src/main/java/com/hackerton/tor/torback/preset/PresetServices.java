package com.hackerton.tor.torback.preset;

import com.hackerton.tor.torback.entity.Preset;
import com.hackerton.tor.torback.repository.PresetRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

@Slf4j
@AllArgsConstructor
@Service
public class PresetServices {

    private PresetRepository presetRepository;


    public Flux<Preset> getPresetListTop20(){
        return this.presetRepository.getPresetListByRecommend()
                .doOnError(error -> log.trace(error.getMessage())).log("getPreset Ranking");
    }

    public Flux<Preset> getMyPresetLists(@NotNull String userId){
        return this.presetRepository.getMyPresetLists(userId)
                .doOnError(error -> log.trace(error.getMessage())).log("get My Presets");
    }
}
