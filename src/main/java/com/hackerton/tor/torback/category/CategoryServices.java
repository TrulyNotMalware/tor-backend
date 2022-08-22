package com.hackerton.tor.torback.category;

import com.hackerton.tor.torback.entity.Category;
import com.hackerton.tor.torback.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@AllArgsConstructor
@Service
public class CategoryServices {

    private final CategoryRepository categoryRepository;

    Flux<Category> getTopCategoryLists(){
        return this.categoryRepository.getTopCategoryLists()
                .doOnError(error -> log.trace(error.getMessage())).log("getTopCategory");
    }

    Flux<Category> getSubCategories(String categoryName){
        return this.categoryRepository.getSubCategires(categoryName)
                .doOnError(error -> log.trace(error.getMessage())).log("getSubCategories");
    }

    Flux<Category> getLowestCategoryLists(){
        return this.categoryRepository.getLowestCategoryList()
                .doOnError(error -> log.trace(error.getMessage())).log("getLowestsCategorty");
    }
}
