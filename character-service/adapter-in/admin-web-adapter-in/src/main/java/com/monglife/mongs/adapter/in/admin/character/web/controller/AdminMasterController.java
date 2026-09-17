package com.monglife.mongs.adapter.in.admin.character.web.controller;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.character.web.dto.response.*;
import com.monglife.mongs.adapter.in.admin.character.web.enums.AdapterInAdminCharacterWebResponse;
import com.monglife.mongs.adapter.in.admin.character.web.dto.request.AdminMasterCreateRequestDto;
import com.monglife.mongs.application.mong.port.in.admin.AdminMongMasterUseCase;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMasterCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 마스터 데이터 읽기 전용. 수정은 SQL 배포로 한다(캐시·시드와 충돌) */
@Validated
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

    /**
     * 마스터 데이터 등록. 종류를 body 의 kind 로 받는다 —
     * 화면이 종류 셀렉트 하나로 폼을 바꿔 쓰는 구조라 경로를 나누지 않았다.
     */
    @EntryLoggingPoint
    @PostMapping
    public ResponseEntity<ResponseDto<Map<String, Object>>> createMaster(@Valid @RequestBody AdminMasterCreateRequestDto requestDto) {

        AdminCreateMasterCommand command = AdminCreateMasterCommand.builder()
                .code(requestDto.getCode())
                .name(requestDto.getName())
                .level(requestDto.getLevel())
                .evolutionScore(requestDto.getEvolutionScore())
                .maxStatus(requestDto.getMaxStatus())
                .groupType(requestDto.getGroupType())
                .price(requestDto.getPrice())
                .weight(requestDto.getWeight())
                .strength(requestDto.getStrength())
                .satiety(requestDto.getSatiety())
                .healthy(requestDto.getHealthy())
                .fatigue(requestDto.getFatigue())
                .delaySeconds(requestDto.getDelaySeconds())
                .payPoint(requestDto.getPayPoint())
                .score(requestDto.getScore())
                .timeout(requestDto.getTimeout())
                .exp(requestDto.getExp())
                .inventoryTypeCode(requestDto.getInventoryTypeCode())
                .build();

        adminMongMasterUseCase.createMasterUseCase(requestDto.getKind(), command);

        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.CREATE_MASTER.toResponseDto(Map.of(
                "kind", requestDto.getKind().name(),
                "code", requestDto.getCode()
        )));
    }

    /** 마스터 데이터 삭제. 표의 행만 지우고 공통 코드는 남긴다 */
    @EntryLoggingPoint
    @DeleteMapping("/{kind}/{id}")
    public ResponseEntity<ResponseDto<Map<String, Object>>> deleteMaster(
            @PathVariable AdminMongMasterUseCase.Kind kind,
            @PathVariable Long id
    ) {
        adminMongMasterUseCase.deleteMasterUseCase(kind, id);
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.DELETE_MASTER.toResponseDto(Map.of("kind", kind.name(), "id", id)));
    }
}
