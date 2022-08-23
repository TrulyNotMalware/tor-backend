package com.hackerton.tor.torback.recommender;


import com.hackerton.tor.torback.entity.Preset;
import com.hackerton.tor.torback.entity.Product;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@Slf4j
@AllArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/recommend")
public class MahoutController {

    private MahoutServices mahoutServices;

    @GetMapping(value = "/getUserRecommendProduct/{userId}", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<?>> getRecommendProductList(
            @PathVariable long userId
    ){
        Mono<Links> allLinks;
        Mono<Link> selfLink = linkTo(methodOn(MahoutController.class).getRecommendProductList(userId))
                .withSelfRel().toMono();
        return Mono.zip(this.mahoutServices.getRecommendedList(userId)
                .collectList(),selfLink)
                .map(objects -> CollectionModel.of(objects.getT1(),objects.getT2()));
    }

    @GetMapping(value = "/getUserRecommendPreset/{userId}", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<?>> getRecommendPresetList(
            @PathVariable long userId
    ){
        Mono<Links> allLinks;
        Mono<Link> selfLink = linkTo(methodOn(MahoutController.class).getRecommendPresetList(userId))
                .withSelfRel().toMono();
        return Mono.zip(this.mahoutServices.getRecommendedPresetList(userId).collectList(),
                selfLink)
                .map(objects -> CollectionModel.of(objects.getT1(),objects.getT2()));
    }
}
