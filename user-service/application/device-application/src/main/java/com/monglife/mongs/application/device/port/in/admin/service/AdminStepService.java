package com.monglife.mongs.application.device.port.in.admin.service;

import com.monglife.mongs.common.admin.log.AdminAuditLog;
import com.monglife.mongs.application.device.port.in.admin.AdminStepUseCase;
import com.monglife.mongs.application.device.port.in.admin.vo.AdminStepVo;
import com.monglife.mongs.application.device.port.out.admin.AdminDeviceCachePort;
import com.monglife.mongs.domain.device.model.Step;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminStepService implements AdminStepUseCase {

    private final AdminDeviceCachePort adminDeviceCachePort;

    @Override
    public AdminStepVo getStepUseCase(Long accountId) {
        return this.toVo(accountId, adminDeviceCachePort.getTodayExchangedWalkingCountPort(accountId));
    }

    @Override
    public AdminStepVo resetStepUseCase(Long accountId) {

        adminDeviceCachePort.resetTodayExchangedWalkingCountPort(accountId);

        AdminAuditLog.write("daily step exchange limit reset accountId={}", accountId);

        return this.toVo(accountId, 0);
    }

    private AdminStepVo toVo(Long accountId, int todayExchangedWalkingCount) {
        return AdminStepVo.builder()
                .accountId(accountId)
                .todayExchangedWalkingCount(todayExchangedWalkingCount)
                .dailyLimit(Step.DAILY_EXCHANGE_LIMIT_WALKING_COUNT)
                .build();
    }
}
