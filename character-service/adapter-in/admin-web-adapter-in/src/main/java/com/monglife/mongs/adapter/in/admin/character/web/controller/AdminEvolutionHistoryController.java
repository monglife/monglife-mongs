package com.monglife.mongs.adapter.in.admin.character.web.controller;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.character.web.dto.response.AdminEvolutionHistoryResponseDto;
import com.monglife.mongs.adapter.in.admin.character.web.enums.AdapterInAdminCharacterWebResponse;
import com.monglife.mongs.application.mong.port.in.admin.AdminMongUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 진화 이력은 계정 단위로만 쌓인다(몽 단위 아님) */
@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminEvolutionHistoryController {

    private final AdminMongUseCase adminMongUseCase;

    @EntryLoggingPoint
    @GetMapping("/{accountId}/evolution-histories")
    public ResponseEntity<ResponseDto<List<AdminEvolutionHistoryResponseDto>>> getEvolutionHistories(@PathVariable Long accountId) {
        List<AdminEvolutionHistoryResponseDto> items = adminMongUseCase.getEvolutionHistoriesUseCase(accountId).stream().map(AdminEvolutionHistoryResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_EVOLUTION_HISTORIES.toResponseDto(items));
    }
}
