package com.monglife.mongs.application.battle.port.in.admin.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** 매치 대기열 관리자 조회 결과. {@code QueuePlayer} 도메인에는 없는 등록 시각을 함께 준다 */
@Getter
public class AdminQueuePlayerVo {

    private final Long mongId;

    private final String deviceId;

    private final Long accountId;

    private final LocalDateTime createdAt;

    @Builder
    public AdminQueuePlayerVo(Long mongId, String deviceId, Long accountId, LocalDateTime createdAt) {
        this.mongId = mongId;
        this.deviceId = deviceId;
        this.accountId = accountId;
        this.createdAt = createdAt;
    }
}
