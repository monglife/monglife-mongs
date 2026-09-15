package com.monglife.mongs.adapter.in.admin.character.web.controller;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.character.web.dto.response.*;
import com.monglife.mongs.adapter.in.admin.character.web.enums.AdapterInAdminCharacterWebResponse;
import com.monglife.mongs.application.mong.port.in.admin.AdminMongMasterUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 마스터 데이터 읽기 전용. 수정은 SQL 배포로 한다(캐시·시드와 충돌) */
@RestController
@RequestMapping("/admin/master")
@RequiredArgsConstructor
public class AdminMasterController {

    private final AdminMongMasterUseCase adminMongMasterUseCase;

    @EntryLoggingPoint
    @GetMapping("/mong-types")
    public ResponseEntity<ResponseDto<List<AdminMongTypeResponseDto>>> getMongTypes() {
        List<AdminMongTypeResponseDto> items = adminMongMasterUseCase.getMongTypesUseCase().stream().map(AdminMongTypeResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_MONG_TYPES.toResponseDto(items));
    }

    @EntryLoggingPoint
    @GetMapping("/foods")
    public ResponseEntity<ResponseDto<List<AdminFeedItemResponseDto>>> getFoods() {
        List<AdminFeedItemResponseDto> items = adminMongMasterUseCase.getFoodsUseCase().stream().map(AdminFeedItemResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_FOODS.toResponseDto(items));
    }

    @EntryLoggingPoint
    @GetMapping("/snacks")
    public ResponseEntity<ResponseDto<List<AdminFeedItemResponseDto>>> getSnacks() {
        List<AdminFeedItemResponseDto> items = adminMongMasterUseCase.getSnacksUseCase().stream().map(AdminFeedItemResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_SNACKS.toResponseDto(items));
    }

    @EntryLoggingPoint
    @GetMapping("/training-types")
    public ResponseEntity<ResponseDto<List<AdminTrainingTypeResponseDto>>> getTrainingTypes() {
        List<AdminTrainingTypeResponseDto> items = adminMongMasterUseCase.getTrainingTypesUseCase().stream().map(AdminTrainingTypeResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_TRAINING_TYPES.toResponseDto(items));
    }

    @EntryLoggingPoint
    @GetMapping("/random-draws")
    public ResponseEntity<ResponseDto<List<AdminRandomDrawResponseDto>>> getRandomDraws() {
        List<AdminRandomDrawResponseDto> items = adminMongMasterUseCase.getRandomDrawsUseCase().stream().map(AdminRandomDrawResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_RANDOM_DRAWS.toResponseDto(items));
    }
}
