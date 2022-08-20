package com.hackerton.tor.torback.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Table(name = "preset")
public class Preset {
    @Id
    @Column("presetId")// Auto-increment
    private int presetId;

    @NotNull
    @Column("presetName")
    private String presetName;

    @Nullable
    @Column("presetContent")
    private String presetContent;

    @NotNull
    @Column("categoryName")
    private String categoryName;

    @NotNull
    @Column("recommend")
    private int recommend;

    @NotNull
    @Column("buyCount")
    private int buyCount;

    @NotNull
    @Column("views")
    private int views;

    @NotNull
    @Column("producer")
    private String producer;

    @Nullable
    @Column("createdAt")
    private LocalDateTime createdAt;

    @Nullable
    @Column("updatedAt")
    private LocalDateTime updatedAt;
}
