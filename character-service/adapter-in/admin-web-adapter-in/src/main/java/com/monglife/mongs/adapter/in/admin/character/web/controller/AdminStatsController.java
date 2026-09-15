package com.monglife.mongs.adapter.in.admin.character.web.controller;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.character.web.dto.response.AdminMongStatsResponseDto;
import com.monglife.mongs.adapter.in.admin.character.web.enums.AdapterInAdminCharacterWebResponse;
import com.monglife.mongs.application.mong.port.in.admin.AdminMongStatsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminMongStatsUseCase adminMongStatsUseCase;

    @EntryLoggingPoint
    @GetMapping
    public ResponseEntity<ResponseDto<AdminMongStatsResponseDto>> getStats() {
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_STATS.toResponseDto(AdminMongStatsResponseDto.of(adminMongStatsUseCase.getStatsUseCase())));
    }
}
