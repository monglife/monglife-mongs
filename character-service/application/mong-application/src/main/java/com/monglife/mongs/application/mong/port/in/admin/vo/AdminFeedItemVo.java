package com.monglife.mongs.application.mong.port.in.admin.vo;

import lombok.Builder;
import lombok.Getter;

/** 음식·간식 마스터 공용 조회 결과 */
@Getter
public class AdminFeedItemVo {

    private final Long id;

    private final String code;

    private final String name;

    private final Integer price;

    private final Double weight;

    private final Double strength;

    private final Double satiety;

    private final Double healthy;

    private final Double fatigue;

    private final Integer delaySeconds;

    @Builder
    public AdminFeedItemVo(Long id, String code, String name, Integer price, Double weight, Double strength, Double satiety, Double healthy, Double fatigue, Integer delaySeconds) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.price = price;
        this.weight = weight;
        this.strength = strength;
        this.satiety = satiety;
        this.healthy = healthy;
        this.fatigue = fatigue;
        this.delaySeconds = delaySeconds;
    }
}
