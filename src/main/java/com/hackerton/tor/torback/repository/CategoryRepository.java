package com.hackerton.tor.torback.repository;


import com.hackerton.tor.torback.entity.Category;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface CategoryRepository extends R2dbcRepository<Category, String> {

    @Query("SELECT * FROM category WHERE parentCategoryName IS null AND categoryName != '프리셋'")
    Flux<Category> getTopCategoryLists();

    @Query("SELECT * FROM category WHERE parentCategoryName = ?")
    Flux<Category> getSubCategires(String categoryName);

    @Query("select * from category\n" +
            "where categoryName not in\n" +
            "(select distinct parentCategoryName from category where parentCategoryName is not null )\n" +
            "AND parentCategoryName != '프리셋'")
    Flux<Category> getLowestCategoryList();
}
