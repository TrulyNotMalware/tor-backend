package com.hackerton.tor.torback.recommender;

import com.hackerton.tor.torback.entity.Product;
import com.hackerton.tor.torback.recommender.model.TORItemBasedRecommender;
import com.hackerton.tor.torback.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class MahoutServices {

    private TORItemBasedRecommender itemBasedRecommender;
    private ProductRepository productRepository;

    //DefaultSizes.
    private final static int numbers = 10;

    public Flux<Product> getItemBasedRecommendedList(long userId){
        return Mono.just(this.itemBasedRecommender.getRecommendedItemsByUserId(userId,numbers))
                .flatMapMany(Flux::fromIterable).log("flatMapMany")
                .flatMap(recommendedItem -> {
                    log.debug("recommendedItem : {}",recommendedItem.getItemID());
                    return this.productRepository.getProductById((int)recommendedItem.getItemID());
                });
    }
}
