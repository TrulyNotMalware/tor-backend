package com.hackerton.tor.torback.recommender;


import com.hackerton.tor.torback.entity.Product;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/recommend")
public class MahoutController {

    private MahoutServices mahoutServices;

    @GetMapping(value = "/getUserRecommend/{userId}", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<?>> getRecommendProductList(
            @PathVariable long userId
    ){
        Mono<Links> allLinks;
        Mono<Link> selfLink = linkTo(methodOn(MahoutController.class).getRecommendProductList(userId))
                .withSelfRel().toMono();
        return Mono.zip(this.mahoutServices.getItemBasedRecommendedList(userId)
                .collectList(),selfLink)
                .map(objects -> CollectionModel.of(objects.getT1(),objects.getT2()));
    }
}
