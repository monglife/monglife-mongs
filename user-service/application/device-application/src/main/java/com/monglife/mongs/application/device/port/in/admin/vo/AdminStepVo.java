package com.monglife.mongs.application.device.port.in.admin.vo;

import lombok.Builder;
import lombok.Getter;

/** 오늘 누적 환전 걸음 수 조회 결과 */
@Getter
public class AdminStepVo {

    private final Long accountId;

    private final Integer todayExchangedWalkingCount;

    private final Integer dailyLimit;

    @Builder
    public AdminStepVo(Long accountId, Integer todayExchangedWalkingCount, Integer dailyLimit) {
        this.accountId = accountId;
        this.todayExchangedWalkingCount = todayExchangedWalkingCount;
        this.dailyLimit = dailyLimit;
    }
}
