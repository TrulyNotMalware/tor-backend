package com.hackerton.tor.torback.preset;

import com.hackerton.tor.torback.entity.Preset;
import com.hackerton.tor.torback.entity.Preset_detail;
import com.hackerton.tor.torback.entity.Preset_score;
import com.hackerton.tor.torback.product.ProductController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.hateoas.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@Slf4j
@AllArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/preset")
public class PresetController {

    private PresetServices services;


    @GetMapping(value = "/getPresetRank", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<EntityModel<Preset>>> getPresetListByRecommend(){
        /**
         * Get Top 20 Ranking preset lists.
         * Required : null
         * Return : Preset Lists
         */
        Mono<Links> allLinks;
        Mono<Link> self = linkTo(methodOn(PresetController.class).getPresetListByRecommend()).withSelfRel().toMono();
        Mono<Link> presetAggregateLink = linkTo(methodOn(PresetController.class).getPresetInfoByName(null))
                .withRel(IanaLinkRelations.ITEM).toMono();

        allLinks = Mono.zip(self,presetAggregateLink)
                .map(links -> Links.of(links.getT1(),links.getT2()));

        Mono<Link> aggregateLink = linkTo(methodOn(ProductController.class).getProductListsByPresetName(null))
                .withRel(IanaLinkRelations.ITEM).toMono();
        return this.services.getPresetListTop20()
                .flatMap(preset -> Mono.just(preset).zipWith(aggregateLink))
                /**
                 * .zipWith is not wait until Flux published, (!= Mono.zip)
                 * So, We have to wait until finished with flatmap
                 */
                .map(objects -> EntityModel.of(objects.getT1(),objects.getT2()))
                .collectList()
                .flatMap(presets -> allLinks
                        .map(link -> CollectionModel.of(presets,link)));
    }
    @GetMapping(value = "/getMyPresetLists/{userId}",produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<EntityModel<Preset>>> getMyPresetList(
            @PathVariable String userId
    ){
        /**
         * Get My Preset Lists
         * Required : userId (Path)
         * Return : Preset Lists
         */
        Mono<Links> allLinks;
        Mono<Link> aggregateLink = linkTo(methodOn(ProductController.class).getProductListsByPresetName(null))
                .withRel(IanaLinkRelations.ITEM).toMono();
        return this.services.getMyPresetLists(userId)
                .flatMap(preset -> Mono.just(preset).zipWith(aggregateLink))
                .map(objects -> EntityModel.of(objects.getT1(),objects.getT2()))
                .collectList()
                .flatMap(presets -> linkTo(methodOn(PresetController.class)
                        .getMyPresetList(userId))
                        .withSelfRel()
                        .toMono()
                        .map(link -> CollectionModel.of(presets,link)));
    }

    @GetMapping(value = "/getEvalPresets/{userId}/{presetId}", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<EntityModel<Preset_score>> getEvalPresetScore(
            @PathVariable String userId,
            @PathVariable int presetId
    ){
        Mono<Links> allLinks;
        Mono<Link> self = linkTo(methodOn(PresetController.class).getEvalPresetScore(userId,presetId)).withSelfRel().toMono();
        Mono<Link> aggregateLink = linkTo(methodOn(ProductController.class).getProductListsByPresetName(null))
                .withRel(IanaLinkRelations.ITEM).toMono();
        allLinks = Mono.zip(self,aggregateLink)
                .map(links -> Links.of(links.getT1(),links.getT2()));
        return this.services.getEvalPresetScores(userId, presetId)
                .flatMap(aDouble -> {
                    Preset_score result = new Preset_score();
                    result.setScore(aDouble);
                    return this.services.getPresetByPresetId(presetId)
                            .flatMap(preset -> {
                                result.setPreset(preset);
                                return Mono.zip(Mono.just(result),allLinks)
                                        .map(objects -> EntityModel.of(objects.getT1(),objects.getT2()));
                            });
                });
    }

    @GetMapping(value = "/getPreset/{presetName}", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<EntityModel<Preset>> getPresetInfoByName(
            @PathVariable String presetName
    ){
        Mono<Link> self = linkTo(methodOn(PresetController.class).getPresetInfoByName(presetName)).withSelfRel().toMono();
        return this.services.getPresetByPresetName(presetName).zipWith(self)
                .map(objects -> EntityModel.of(objects.getT1(),objects.getT2()));
    }

    @PutMapping(value = "/updatePresetRecommend", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<Preset> updatePresetRecommendByPresetId(
            @RequestBody HashMap<String, String> params
    ){
        int presetId = Integer.parseInt(params.get("presetId"));
        return this.services.updateRecommendByPresetId(presetId);
    }

    @PostMapping(value = "/updatePurchaseHistory",produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<?>> updatePurchaseHistory(
            @RequestBody HashMap<String, Object> params
    ){
        /**
         * Example format of params
         * {
         *     "userId":"freddie",
         *     "presetId":1,
         *     "items":{//productId : Count
         *         "102":100,
         *         "93":18
         *     }
         * }
         */
        String userId = String.valueOf(params.get("userId"));
        long presetId = Long.parseLong(String.valueOf(params.get("presetId")));

        Mono<Link> selfLink = linkTo(methodOn(PresetController.class).updatePurchaseHistory(params))
                .withSelfRel().toMono();
        Map<String,Integer> items = (HashMap<String,Integer>) params.get("items");

        return Mono.zip(this.services.updatePurchaseHistory(userId,presetId,items),selfLink)
                .map(objects -> CollectionModel.of(objects.getT1(),objects.getT2()));
    }


    @PostMapping(value = "/createPreset", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<?>> createPreset(
            @RequestBody HashMap<String, Object> params
    ){
        /**
         * {
         *     "presetName":String,
         *     "presetContent":String,
         *     "presetCategoryName":presetCategory,
         *     "producer": userId,
         *     "items":{
         *         productCategoryName:[productId,productId...],
         *     }
         * }
         */
        String presetName = String.valueOf(params.get("presetName"));
        String presetContent = String.valueOf(params.get("presetContent"));
        String presetCategoryName = String.valueOf(params.get("presetCategoryName"));
        String producer = String.valueOf(params.get("producer"));
        Map<String,String> items = (Map<String, String>) params.get("items");

        Mono<Link> selfLink = linkTo(methodOn(PresetController.class).createPreset(params))
                .withSelfRel().toMono();

        return Mono.zip(this.services.createPreset(presetName,presetContent,presetCategoryName,producer,items),selfLink)
                .map(objects -> CollectionModel.of(objects.getT1(),objects.getT2()));
    }

}