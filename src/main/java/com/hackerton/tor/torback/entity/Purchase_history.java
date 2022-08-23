package com.hackerton.tor.torback.entity;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Table(name = "purchase_history")
public class Purchase_history {
    @NotNull
    @Column("userId")//FK
    private String userId;

    @NotNull
    @Column("productId")//FK
    private long productId;

    @NotNull
    @Column("count")
    private int count;

    @Nullable
    @Column("createdAt")
    private LocalDateTime createdAt;

    @Nullable
    @Column("updatedAt")
    private LocalDateTime updatedAt;
}
