package com.hackerton.tor.torback.repository;


import com.hackerton.tor.torback.entity.Category;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CategoryRepository extends R2dbcRepository<Category, String> {
}
