package com.hackerton.tor.torback.entity;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

@Data
@Table(name = "user_filter_binding")
public class User_filter_binding {
    @Nullable
    @Column("userId")//FK
    private String userId;

    @Nullable
    @Column("presetId")//FK
    private long presetId;

    @NotNull
    @Column("recommend")
    private int recommend;

    @NotNull
    @Column("buyCount")
    private int buyCount;
}
