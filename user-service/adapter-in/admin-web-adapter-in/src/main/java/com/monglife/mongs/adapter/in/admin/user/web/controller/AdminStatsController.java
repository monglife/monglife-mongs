package com.monglife.mongs.adapter.in.admin.user.web.controller;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.user.web.dto.response.AdminMemberStatsResponseDto;
import com.monglife.mongs.adapter.in.admin.user.web.enums.AdapterInAdminUserWebResponse;
import com.monglife.mongs.application.member.port.in.admin.AdminMemberStatsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminMemberStatsUseCase adminMemberStatsUseCase;

    @EntryLoggingPoint
    @GetMapping
    public ResponseEntity<ResponseDto<AdminMemberStatsResponseDto>> getStats() {
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.GET_STATS.toResponseDto(AdminMemberStatsResponseDto.of(adminMemberStatsUseCase.getStatsUseCase())));
    }
}
