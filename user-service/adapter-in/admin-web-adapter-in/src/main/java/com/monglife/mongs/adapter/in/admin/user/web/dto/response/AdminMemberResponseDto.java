package com.monglife.mongs.adapter.in.admin.user.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMemberVo;
import com.monglife.mongs.domain.member.model.Player;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminMemberResponseDto(
        Long accountId,
        Integer slotCount,
        Integer starPoint,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
        LocalDateTime updatedAt
) {
    public static AdminMemberResponseDto of(AdminMemberVo vo) {
        return AdminMemberResponseDto.builder()
                .accountId(vo.getAccountId())
                .slotCount(vo.getSlotCount())
                .starPoint(vo.getStarPoint())
                .createdAt(vo.getCreatedAt())
                .updatedAt(vo.getUpdatedAt())
                .build();
    }

    public static AdminMemberResponseDto of(Player player) {
        return AdminMemberResponseDto.builder()
                .accountId(player.getAccountId())
                .slotCount(player.getSlotCount())
                .starPoint(player.getStarPoint())
                .build();
    }
}
