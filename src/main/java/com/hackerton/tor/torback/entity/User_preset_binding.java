package com.hackerton.tor.torback.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Table(name = "user_preset_binding")
public class User_preset_binding {
    @NotNull
    @Column("userId") //FK
    private String userId;

    @NotNull
    @Column("presetId") //FK
    private long presetId;

    @NotNull
    @Column("recommend")
    private int recommend;

    @NotNull
    @Column("buyCount")
    private int buyCount;

    @Nullable
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column("createdAt")
    private LocalDateTime createdAt;

    @Nullable
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column("updatedAt")
    private LocalDateTime updatedAt;
}
