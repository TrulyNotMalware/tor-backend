package com.hackerton.tor.torback.recommender;

import com.hackerton.tor.torback.entity.Preset;
import com.hackerton.tor.torback.entity.Product;
import com.hackerton.tor.torback.recommender.model.TORItemBasedRecommender;
import com.hackerton.tor.torback.recommender.model.TORUserBasedRecommender;
import com.hackerton.tor.torback.repository.PresetRepository;
import com.hackerton.tor.torback.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
@Service
public class MahoutServices {

    private TORItemBasedRecommender itemBasedRecommender;
    private TORUserBasedRecommender userBasedRecommender;
    private ProductRepository productRepository;
    private PresetRepository presetRepository;

    //DefaultSizes.
    private final static int numbers = 5;

    public Flux<Product> getRecommendedList(long userId){
        return Mono.zip(Mono.just(this.itemBasedRecommender.getRecommendedItemsByUserId(userId,numbers)),
                Mono.just(this.userBasedRecommender.getRecommendedItemsByUserId(userId,numbers)))
                .flatMap(tuple2 -> {
                    tuple2.getT1().forEach(recommendedItem -> {
                        log.debug("recommendedItem1 : {}",recommendedItem.getItemID());
                    });
                    tuple2.getT2().forEach(recommendedItem -> {
                        log.debug("recommendedItem2 : {}",recommendedItem.getItemID());
                    });
                    List<RecommendedItem> itemsList = Stream.concat(tuple2.getT1().stream(),tuple2.getT2().stream())
                            .distinct().collect(Collectors.toList());
                    return Mono.just(itemsList);
                })
                .flatMapMany(Flux::fromIterable).log("flatMapMany")
                .flatMap(recommendedItem -> {
                    log.debug("recommendedItem : {}",recommendedItem.getItemID());
                    return this.productRepository.getProductById((int)recommendedItem.getItemID());
                }).switchIfEmpty(Flux.defer(Flux::empty));

//        return Mono.just(this.itemBasedRecommender.getRecommendedItemsByUserId(userId,numbers))
//                .flatMapMany(Flux::fromIterable).log("flatMapMany")
//                .flatMap(recommendedItem -> {
//                    log.debug("recommendedItem : {}",recommendedItem.getItemID());
//                    return this.productRepository.getProductById((int)recommendedItem.getItemID());
//                });
    }

    public Flux<Preset> getRecommendedPresetList(long userId){
        return Mono.zip(Mono.just(this.itemBasedRecommender.getRecommendedPresetItemByUserId(userId,numbers))
        ,Mono.just(this.userBasedRecommender.getPresetRecommendedItemsByUserId(userId,numbers)))
                .flatMap(tuple2 -> {
                    List<RecommendedItem> itemsList;
                    tuple2.getT1().forEach(recommendedItem -> {
                        log.debug("recommendedItem1 : {}",recommendedItem.getItemID());
                    });
                    tuple2.getT2().forEach(recommendedItem -> {
                        log.debug("recommendedItem2 : {}",recommendedItem.getItemID());
                    });
                    for(RecommendedItem item :tuple2.getT1()){
                        tuple2.getT2().removeIf(item2 -> item.getItemID() == item2.getItemID());
                    }
                    itemsList = Stream.concat(tuple2.getT1().stream(),tuple2.getT2().stream())
                            .distinct().collect(Collectors.toList());
                    return Mono.just(itemsList);
                }).flatMapMany(Flux::fromIterable).log("presetFlatMapMany")
                .flatMap(recommendedItem -> {
                    log.debug("recommendedItem : {}",recommendedItem.getItemID());
                    return this.presetRepository.getPresetById((int)recommendedItem.getItemID());
                }).switchIfEmpty(Flux.defer(Flux::empty));
    }
}
