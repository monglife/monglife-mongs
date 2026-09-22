package com.monglife.mongs.adapter.in.admin.user.web.dto.response;

import com.monglife.mongs.application.device.port.in.admin.vo.AdminStepVo;

public record AdminStepResponseDto(Long accountId, Integer todayExchangedWalkingCount, Integer dailyLimit) {

    public static AdminStepResponseDto of(AdminStepVo vo) {
        return new AdminStepResponseDto(vo.getAccountId(), vo.getTodayExchangedWalkingCount(), vo.getDailyLimit());
    }
}
