package com.monglife.mongs.application.mong.port.in.admin.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminUpdateMongStatusCommand {

    private final Long mongId;

    private final Double weight;

    private final Double strength;

    private final Double satiety;

    private final Double healthy;

    private final Double fatigue;

    private final Double exp;

    private final Integer payPoint;

    private final Integer poopCount;

    private final Integer randomDrawTicketCount;

    @Builder
    public AdminUpdateMongStatusCommand(Long mongId, Double weight, Double strength, Double satiety, Double healthy, Double fatigue, Double exp, Integer payPoint, Integer poopCount, Integer randomDrawTicketCount) {
        this.mongId = mongId;
        this.weight = weight;
        this.strength = strength;
        this.satiety = satiety;
        this.healthy = healthy;
        this.fatigue = fatigue;
        this.exp = exp;
        this.payPoint = payPoint;
        this.poopCount = poopCount;
        this.randomDrawTicketCount = randomDrawTicketCount;
    }
}
