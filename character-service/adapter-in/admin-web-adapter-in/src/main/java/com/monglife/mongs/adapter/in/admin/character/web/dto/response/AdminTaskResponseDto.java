package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminTaskVo;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
public record AdminTaskResponseDto(
        Long taskId,
        Long mongId,
        Long accountId,
        String schedulerTypeCode,
        String stateCode,
        String typeCode,
        Long expirationSeconds,
        Long restExpirationSeconds,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
        LocalDateTime expiredAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
        LocalTime fixTime,
        Boolean isScheduled,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
        LocalDateTime updatedAt
) {
    public static AdminTaskResponseDto of(AdminTaskVo vo) {
        return AdminTaskResponseDto.builder()
                .taskId(vo.getTaskId())
                .mongId(vo.getMongId())
                .accountId(vo.getAccountId())
                .schedulerTypeCode(vo.getSchedulerTypeCode())
                .stateCode(vo.getStateCode())
                .typeCode(vo.getTypeCode())
                .expirationSeconds(vo.getExpirationSeconds())
                .restExpirationSeconds(vo.getRestExpirationSeconds())
                .expiredAt(vo.getExpiredAt())
                .fixTime(vo.getFixTime())
                .isScheduled(vo.getIsScheduled())
                .createdAt(vo.getCreatedAt())
                .updatedAt(vo.getUpdatedAt())
                .build();
    }
}
