package com.monglife.mongs.application.member.port.in.admin.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminExchangeStarPointProductVo {

    private final String productId;

    private final String productName;

    private final Integer starPoint;

    @Builder
    public AdminExchangeStarPointProductVo(String productId, String productName, Integer starPoint) {
        this.productId = productId;
        this.productName = productName;
        this.starPoint = starPoint;
    }
}
