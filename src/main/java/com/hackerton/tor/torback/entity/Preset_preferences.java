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
@Table(name = "preset_preferences")
public class Preset_preferences {

    @NotNull
    @Column("userNumber")
    private long userNumber;

    @NotNull
    @Column("presetId")
    private long presetId;

    @NotNull
    @Column("preference")
    private float preference;

    @Nullable
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column("createdAt")
    private LocalDateTime createdAt;
}
