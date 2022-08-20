package com.hackerton.tor.torback.entity;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Table(name = "preset_detail")
public class Preset_detail {

    @NotNull
    @Column("presetId")//FK
    private int presetId;

    @NotNull
    @Column("categoryName")//FK
    private String categoryName;

    @NotNull
    @Column("productId")//FK
    private int productId;

    @Nullable
    @Column("createdAt")
    private LocalDateTime createdAt;

    @Nullable
    @Column("updatedAt")
    private LocalDateTime updatedAt;
}
