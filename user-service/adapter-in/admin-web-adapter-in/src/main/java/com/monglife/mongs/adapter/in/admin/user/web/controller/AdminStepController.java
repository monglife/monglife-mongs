package com.monglife.mongs.adapter.in.admin.user.web.controller;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.user.web.dto.response.AdminStepResponseDto;
import com.monglife.mongs.adapter.in.admin.user.web.enums.AdapterInAdminUserWebResponse;
import com.monglife.mongs.application.device.port.in.admin.AdminStepUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 걸음 수 환전 일일 상한 조회·초기화 */
@RestController
@RequestMapping("/admin/steps")
@RequiredArgsConstructor
public class AdminStepController {

    private final AdminStepUseCase adminStepUseCase;

    @EntryLoggingPoint
    @GetMapping("/{accountId}")
    public ResponseEntity<ResponseDto<AdminStepResponseDto>> getStep(@PathVariable Long accountId) {
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.GET_STEP.toResponseDto(AdminStepResponseDto.of(adminStepUseCase.getStepUseCase(accountId))));
    }

    @EntryLoggingPoint
    @DeleteMapping("/{accountId}")
    public ResponseEntity<ResponseDto<AdminStepResponseDto>> resetStep(@PathVariable Long accountId) {
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.RESET_STEP.toResponseDto(AdminStepResponseDto.of(adminStepUseCase.resetStepUseCase(accountId))));
    }
}
