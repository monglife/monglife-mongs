package com.monglife.mongs.adapter.in.admin.user.web.dto.response;

import com.monglife.mongs.application.member.port.in.admin.vo.AdminMemberStatsVo;
import lombok.Builder;

@Builder
public record AdminMemberStatsResponseDto(
        Long totalMembers,
        Long todayJoined,
        Long totalStarPoint,
        Long totalOrders,
        Long todayOrders,
        Double todayOrderAmount
) {
    public static AdminMemberStatsResponseDto of(AdminMemberStatsVo vo) {
        return AdminMemberStatsResponseDto.builder()
                .totalMembers(vo.getTotalMembers())
                .todayJoined(vo.getTodayJoined())
                .totalStarPoint(vo.getTotalStarPoint())
                .totalOrders(vo.getTotalOrders())
                .todayOrders(vo.getTodayOrders())
                .todayOrderAmount(vo.getTodayOrderAmount())
                .build();
    }
}
