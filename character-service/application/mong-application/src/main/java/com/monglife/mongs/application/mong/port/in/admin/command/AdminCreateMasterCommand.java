package com.monglife.mongs.application.mong.port.in.admin.command;

import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import lombok.Builder;
import lombok.Getter;

/**
 * 마스터 데이터 등록. 종류마다 쓰는 필드가 달라 한 커맨드에 모았다.
 *
 * <p>{@code code}/{@code name} 은 공통 코드(monglife_comn_code) 가 된다. 그룹 코드는 종류가 정하므로
 * 따로 받지 않는다. 랜덤 뽑기만 예외로, 이미 있는 음식·간식·맵 코드를 그대로 쓴다.
 */
@Getter
public class AdminCreateMasterCommand {

    private final String code;

    private final String name;

    /* 몽 타입 */
    private final Integer level;
    private final Double evolutionScore;
    private final Double maxStatus;
    private final String groupType;

    /* 음식·간식 */
    private final Integer price;
    private final Double weight;
    private final Double strength;
    private final Double satiety;
    private final Double healthy;
    private final Double fatigue;
    private final Integer delaySeconds;

    /* 훈련 */
    private final Integer payPoint;
    private final Integer score;
    private final Integer timeout;
    private final Double exp;

    /* 랜덤 뽑기 */
    private final InventoryTypeCode inventoryTypeCode;

    @Builder
    public AdminCreateMasterCommand(String code, String name, Integer level, Double evolutionScore, Double maxStatus, String groupType,
                                    Integer price, Double weight, Double strength, Double satiety, Double healthy, Double fatigue, Integer delaySeconds,
                                    Integer payPoint, Integer score, Integer timeout, Double exp, InventoryTypeCode inventoryTypeCode) {
        this.code = code;
        this.name = name;
        this.level = level;
        this.evolutionScore = evolutionScore;
        this.maxStatus = maxStatus;
        this.groupType = groupType;
        this.price = price;
        this.weight = weight;
        this.strength = strength;
        this.satiety = satiety;
        this.healthy = healthy;
        this.fatigue = fatigue;
        this.delaySeconds = delaySeconds;
        this.payPoint = payPoint;
        this.score = score;
        this.timeout = timeout;
        this.exp = exp;
        this.inventoryTypeCode = inventoryTypeCode;
    }
}
