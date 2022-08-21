package com.hackerton.tor.torback.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class Preset_score {

    @NotNull
    private Preset preset;

    @NotNull
    private Double score;
}
