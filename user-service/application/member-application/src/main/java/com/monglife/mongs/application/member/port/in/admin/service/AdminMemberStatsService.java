package com.monglife.mongs.application.member.port.in.admin.service;

import com.monglife.mongs.application.member.port.in.admin.AdminMemberStatsUseCase;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMemberStatsVo;
import com.monglife.mongs.application.member.port.out.admin.AdminMemberReadPort;
import com.monglife.mongs.application.member.port.out.admin.AdminOrderReadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminMemberStatsService implements AdminMemberStatsUseCase {

    private final AdminMemberReadPort adminMemberReadPort;

    private final AdminOrderReadPort adminOrderReadPort;

    @Override
    @Transactional
    public AdminMemberStatsVo getStatsUseCase() {

        LocalDateTime today = LocalDate.now().atStartOfDay();

        return AdminMemberStatsVo.builder()
                .totalMembers(adminMemberReadPort.countMembersPort())
                .todayJoined(adminMemberReadPort.countMembersJoinedSincePort(today))
                .totalStarPoint(adminMemberReadPort.sumStarPointPort())
                .totalOrders(adminOrderReadPort.countOrdersPort())
                .todayOrders(adminOrderReadPort.countOrdersSincePort(today))
                .todayOrderAmount(adminOrderReadPort.sumOrderPriceSincePort(today))
                .build();
    }
}
