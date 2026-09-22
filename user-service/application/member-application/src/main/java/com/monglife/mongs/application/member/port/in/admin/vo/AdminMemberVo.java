package com.monglife.mongs.application.member.port.in.admin.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자용 멤버 조회 결과. {@code Player} 도메인에는 없는 등록·수정 시각을 함께 준다.
 */
@Getter
public class AdminMemberVo {

    private final Long accountId;

    private final Integer slotCount;

    private final Integer starPoint;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    @Builder
    public AdminMemberVo(Long accountId, Integer slotCount, Integer starPoint, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.accountId = accountId;
        this.slotCount = slotCount;
        this.starPoint = starPoint;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
