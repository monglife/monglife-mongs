package com.monglife.mongs.application.mong.port.in.admin.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminMongTypeVo {

    private final Long mongTypeId;

    private final String mongCode;

    private final String mongName;

    private final Integer level;

    private final Double evolutionScore;

    private final Double maxStatus;

    private final String groupType;

    @Builder
    public AdminMongTypeVo(Long mongTypeId, String mongCode, String mongName, Integer level, Double evolutionScore, Double maxStatus, String groupType) {
        this.mongTypeId = mongTypeId;
        this.mongCode = mongCode;
        this.mongName = mongName;
        this.level = level;
        this.evolutionScore = evolutionScore;
        this.maxStatus = maxStatus;
        this.groupType = groupType;
    }
}
