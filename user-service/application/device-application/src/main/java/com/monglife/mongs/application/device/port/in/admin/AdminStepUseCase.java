package com.monglife.mongs.application.device.port.in.admin;

import com.monglife.mongs.application.device.port.in.admin.vo.AdminStepVo;

public interface AdminStepUseCase {

    AdminStepVo getStepUseCase(Long accountId);

    /** 일일 환전 상한 초기화 */
    AdminStepVo resetStepUseCase(Long accountId);
}
