package com.hackerton.tor.torback.category;

import com.hackerton.tor.torback.entity.Category;
import com.hackerton.tor.torback.product.ProductController;
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
@RequestMapping("/api/category")
public class CategoryController {

    private CategoryServices categoryServices;

    @GetMapping(value = "/getCategories", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<EntityModel<Category>>> getTopCategoryLists(){

        Mono<Links> allLinks;
        Mono<Link> self = linkTo(methodOn(CategoryController.class).getTopCategoryLists()).withSelfRel().toMono();
        Mono<Link> categoryAggregateLink = linkTo(methodOn(CategoryController.class).getSubCategories(null))
                .withRel(IanaLinkRelations.ITEM).toMono();

        Mono<Link> aggregateLink = linkTo(methodOn(ProductController.class).getProductListsByCategoryName(null))
                .withRel(IanaLinkRelations.ITEM).toMono();

        allLinks = Mono.zip(self,categoryAggregateLink)
                .map(links -> Links.of(links.getT1(),links.getT2()));

        return this.categoryServices.getTopCategoryLists()
                .flatMap(category -> Mono.just(category).zipWith(aggregateLink))
                .map(objects -> EntityModel.of(objects.getT1(),objects.getT2()))
                .collectList()
                .flatMap(categories -> allLinks.map(
                        links -> CollectionModel.of(categories,links)
                ));
    }

    @GetMapping(value = "/getCategories/{parentCategoryName}",produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<EntityModel<Category>>> getSubCategories(
            @PathVariable String parentCategoryName
    ){
        Mono<Links> allLinks;
        Mono<Link> self = linkTo(methodOn(CategoryController.class).getSubCategories(parentCategoryName)).withSelfRel().toMono();

        Mono<Link> aggregateLink = linkTo(methodOn(ProductController.class).getProductListsByCategoryName(null))
                .withRel(IanaLinkRelations.ITEM).toMono();
        return this.categoryServices.getSubCategories(parentCategoryName)
                .flatMap(category -> Mono.just(category).zipWith(aggregateLink))
                .map(objects -> EntityModel.of(objects.getT1(),objects.getT2()))
                .collectList()
                .flatMap(categories -> self.map(
                        link -> CollectionModel.of(categories,link)
                ));
    }


    @GetMapping(value = "/getCategories/lowest", produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<?>> getLowestCategoryLists(){

        Mono<Links> allLinks;
        Mono<Link> self = linkTo(methodOn(CategoryController.class).getLowestCategoryLists()).withSelfRel().toMono();
        Mono<Link> aggregateLink = linkTo(methodOn(ProductController.class).getProductListsByCategoryName(null))
                .withRel(IanaLinkRelations.ITEM).toMono();
        allLinks = Mono.zip(self,aggregateLink)
                .map(links -> Links.of(links.getT1(),links.getT2()));

        return Mono.zip(this.categoryServices.getLowestCategoryLists().collectList(),allLinks)
                .map(objects -> CollectionModel.of(objects.getT1(),objects.getT2()));
    }

    @GetMapping(value = "/getAllPresetCategories",produces = MediaTypes.HAL_JSON_VALUE)
    public Mono<CollectionModel<?>> getAllPresetCategories(){
        Mono<Links> allLinks;
        Mono<Link> self = linkTo(methodOn(CategoryController.class).getAllPresetCategories()).withSelfRel().toMono();

        return Mono.zip(this.categoryServices.getPresetCategories().collectList(),self)
                .map(objects -> CollectionModel.of(objects.getT1(),objects.getT2()));
    }

}
