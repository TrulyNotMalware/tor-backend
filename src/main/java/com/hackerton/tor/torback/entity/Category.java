package com.hackerton.tor.torback.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Table(name = "category") //Category Table
public class Category {
    @Id//Primary Key
    @Column("categoryName")
    private String categoryName;

    @Nullable
    @Column("parentCategoryName")
    private String parentCategoryName;

    @Nullable
    @Column("createdAt")
    private LocalDateTime createdAt;

    @Nullable
    @Column("updatedAt")
    private LocalDateTime updatedAt;
}
