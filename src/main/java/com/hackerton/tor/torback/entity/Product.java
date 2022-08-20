package com.hackerton.tor.torback.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Table(name = "product")
public class Product {
    @Id
    @Column("productId") //Auto Increments
    private int productId;

    @NotNull
    @Column("productName")
    private String productName;

    @Nullable
    @Column("categoryName")
    private String categoryName;

    @NotNull
    @Column("company")
    private String company;

    @NotNull
    @Column("price")
    private int price;

    @Nullable
    @Column("weight")
    private int weight;

    @NotNull
    @Column("score")
    private float score;

    @NotNull
    @Column("imagePath")
    private String imagePath;

    @Nullable
    @Column("createdAt")
    private LocalDateTime createdAt;

    @Nullable
    @Column("updatedAt")
    private LocalDateTime updatedAt;
}
