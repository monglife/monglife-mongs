package com.monglife.mongs.adapter.in.admin.character.web.controller;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.character.web.dto.request.AdminMissionCreateRequestDto;
import com.monglife.mongs.adapter.in.admin.character.web.dto.response.AdminAccountMissionResponseDto;
import com.monglife.mongs.adapter.in.admin.character.web.dto.response.AdminMissionResponseDto;
import com.monglife.mongs.adapter.in.admin.character.web.enums.AdapterInAdminCharacterWebResponse;
import com.monglife.mongs.application.mong.port.in.admin.AdminMissionUseCase;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMissionCommand;
import com.monglife.mongs.domain.mission.model.MissionReward;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 미션 마스터 운영.
 *
 * <p>{@code /admin/master} 의 kind 방식에 태우지 않았다 - 미션은 리워드가 중첩 목록이라
 * 평면 union 요청 DTO 로 표현되지 않고, 공통 코드도 만들지 않아 그쪽 등록 경로와 공유할 처리가 없다.
 */
@Validated
@RestController
@RequestMapping("/admin/missions")
@RequiredArgsConstructor
public class AdminMissionController {

    private final AdminMissionUseCase adminMissionUseCase;

    /** 미션 마스터 목록. 비활성도 포함한다 */
    @EntryLoggingPoint
    @GetMapping
    public ResponseEntity<ResponseDto<List<AdminMissionResponseDto>>> getMissions() {

        List<AdminMissionResponseDto> items = adminMissionUseCase.getMissionsUseCase().stream()
                .map(AdminMissionResponseDto::of)
                .toList();

        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_MISSIONS.toResponseDto(items));
    }

    /** 미션 등록 */
    @EntryLoggingPoint
    @PostMapping
    public ResponseEntity<ResponseDto<AdminMissionResponseDto>> createMission(@Valid @RequestBody AdminMissionCreateRequestDto requestDto) {

        AdminCreateMissionCommand command = AdminCreateMissionCommand.builder()
                .missionCode(requestDto.getMissionCode())
                .cycleCode(requestDto.getCycleCode())
                .actionCode(requestDto.getActionCode())
                .goalTypeCode(requestDto.getGoalTypeCode())
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .goalCount(requestDto.getGoalCount())
                .isActive(requestDto.getIsActive())
                .sortOrder(requestDto.getSortOrder())
                .rewards(requestDto.getRewards().stream()
                        .map(reward -> MissionReward.builder()
                                .rewardTypeCode(reward.getRewardTypeCode())
                                .rewardCode(reward.getRewardCode())
                                .inventoryTypeCode(reward.getInventoryTypeCode())
                                .amount(reward.getAmount())
                                .build())
                        .toList())
                .build();

        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.CREATE_MISSION
                .toResponseDto(AdminMissionResponseDto.of(adminMissionUseCase.createMissionUseCase(command))));
    }

    /** 미션 삭제. 사용자에게 이미 적재된 미션은 지울 수 없다(409) - 노출만 멈추려면 is_active 를 내린다 */
    @EntryLoggingPoint
    @DeleteMapping("/{missionId}")
    public ResponseEntity<ResponseDto<Map<String, Object>>> deleteMission(@PathVariable("missionId") @NotNull @Min(1) Long missionId) {

        adminMissionUseCase.deleteMissionUseCase(missionId);

        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.DELETE_MISSION.toResponseDto(Map.of("missionId", missionId)));
    }

    /** 특정 계정의 이번 주기 미션 진행 현황 */
    @EntryLoggingPoint
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<ResponseDto<List<AdminAccountMissionResponseDto>>> getAccountMissions(@PathVariable("accountId") @NotNull @Min(1) Long accountId) {

        List<AdminAccountMissionResponseDto> items = adminMissionUseCase.getAccountMissionsUseCase(accountId).stream()
                .map(AdminAccountMissionResponseDto::of)
                .toList();

        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_ACCOUNT_MISSIONS.toResponseDto(items));
    }
}
