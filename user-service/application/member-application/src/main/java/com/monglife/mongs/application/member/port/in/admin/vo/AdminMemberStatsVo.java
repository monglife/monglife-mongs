package com.monglife.mongs.application.member.port.in.admin.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminMemberStatsVo {

    private final Long totalMembers;

    private final Long todayJoined;

    private final Long totalStarPoint;

    private final Long totalOrders;

    private final Long todayOrders;

    private final Double todayOrderAmount;

    @Builder
    public AdminMemberStatsVo(Long totalMembers, Long todayJoined, Long totalStarPoint, Long totalOrders, Long todayOrders, Double todayOrderAmount) {
        this.totalMembers = totalMembers;
        this.todayJoined = todayJoined;
        this.totalStarPoint = totalStarPoint;
        this.totalOrders = totalOrders;
        this.todayOrders = todayOrders;
        this.todayOrderAmount = todayOrderAmount;
    }
}
