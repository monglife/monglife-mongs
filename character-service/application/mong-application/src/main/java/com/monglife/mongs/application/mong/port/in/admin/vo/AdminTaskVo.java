package com.monglife.mongs.application.mong.port.in.admin.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

/** 몽 스케줄(task) 관리자 조회 결과. 상태 코드는 스케줄 어댑터의 enum 이름 그대로 */
@Getter
public class AdminTaskVo {

    private final Long taskId;

    private final Long mongId;

    private final Long accountId;

    private final String schedulerTypeCode;

    private final String stateCode;

    private final String typeCode;

    private final Long expirationSeconds;

    private final Long restExpirationSeconds;

    private final LocalDateTime expiredAt;

    private final LocalTime fixTime;

    private final Boolean isScheduled;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    @Builder
    public AdminTaskVo(Long taskId, Long mongId, Long accountId, String schedulerTypeCode, String stateCode, String typeCode, Long expirationSeconds, Long restExpirationSeconds, LocalDateTime expiredAt, LocalTime fixTime, Boolean isScheduled, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.taskId = taskId;
        this.mongId = mongId;
        this.accountId = accountId;
        this.schedulerTypeCode = schedulerTypeCode;
        this.stateCode = stateCode;
        this.typeCode = typeCode;
        this.expirationSeconds = expirationSeconds;
        this.restExpirationSeconds = restExpirationSeconds;
        this.expiredAt = expiredAt;
        this.fixTime = fixTime;
        this.isScheduled = isScheduled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
