package com.monglife.mongs.adapter.in.mong.web.controller;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.module.common.security.principal.Passport;
import com.monglife.mongs.adapter.in.mong.web.dto.request.ClaimMissionRewardRequestDto;
import com.monglife.mongs.adapter.in.mong.web.dto.response.ClaimMissionRewardResponseDto;
import com.monglife.mongs.adapter.in.mong.web.dto.response.GetMissionResponseDto;
import com.monglife.mongs.adapter.in.mong.web.dto.response.GetMissionRewardResponseDto;
import com.monglife.mongs.adapter.in.mong.web.enums.AdapterInMongWebResponse;
import com.monglife.mongs.application.mong.port.in.MissionUseCase;
import com.monglife.mongs.application.mong.port.in.command.ClaimMissionRewardCommand;
import com.monglife.mongs.application.mong.port.in.command.GetMissionsCommand;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mong.model.Mong;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/mission")
@RequiredArgsConstructor
public class MissionController {

    private final MissionUseCase missionUseCase;

    /**
     * 미션 목록 조회.
     *
     * <p>오늘·이번 주·이번 달 미션을 한 번에 돌려준다. 해당 주기 미션이 아직 없으면 이 호출에서 적재된다 -
     * 조회지만 쓰기가 일어난다. 일간 5개는 이때 뽑혀 고정되고, 같은 날 다시 불러도 바뀌지 않는다.
     *
     * @return 미션 목록
     */
    @EntryLoggingPoint
    @GetMapping
    public ResponseEntity<ResponseDto<List<GetMissionResponseDto>>> getMissions(@AuthenticationPrincipal Passport passport) {

        GetMissionsCommand command = GetMissionsCommand.builder()
                .accountId(passport.getAccountId())
                .build();

        List<GetMissionResponseDto> getMissionResponseDtos = missionUseCase.getMissionsUseCase(command).stream()
                .map(MissionController::toResponseDto)
                .toList();

        return ResponseEntity.ok(AdapterInMongWebResponse.GET_MISSIONS.toResponseDto(getMissionResponseDtos));
    }

    /**
     * 미션 리워드 수령
     *
     * @param accountMissionId 사용자 미션 ID
     * @return 리워드 반영 후 몽 정보
     */
    @EntryLoggingPoint
    @PostMapping("/{accountMissionId}/claim")
    public ResponseEntity<ResponseDto<ClaimMissionRewardResponseDto>> claimMissionReward(
            @AuthenticationPrincipal Passport passport,
            @PathVariable("accountMissionId") @NotNull @Min(1) Long accountMissionId,
            @Valid @RequestBody ClaimMissionRewardRequestDto requestDto
    ) {
        ClaimMissionRewardCommand command = ClaimMissionRewardCommand.builder()
                .accountId(passport.getAccountId())
                .accountMissionId(accountMissionId)
                .mongId(requestDto.getMongId())
                .build();

        Mong mong = missionUseCase.claimMissionRewardUseCase(command);

        ClaimMissionRewardResponseDto claimMissionRewardResponseDto = ClaimMissionRewardResponseDto.builder()
                .accountMissionId(accountMissionId)
                .mongId(mong.getMongId())
                .expRatio(mong.getExp() / mong.getMaxStatus() * 100)
                .payPoint(mong.getPayPoint())
                .updatedAt(mong.getUpdatedAt())
                .build();

        return ResponseEntity.ok(AdapterInMongWebResponse.CLAIM_MISSION_REWARD.toResponseDto(claimMissionRewardResponseDto));
    }

    private static GetMissionResponseDto toResponseDto(AccountMission accountMission) {
        return GetMissionResponseDto.builder()
                .accountMissionId(accountMission.getAccountMissionId())
                .missionCode(accountMission.getMission().getMissionCode())
                .cycleCode(accountMission.getMission().getCycleCode())
                .goalTypeCode(accountMission.getMission().getGoalTypeCode())
                .title(accountMission.getMission().getTitle())
                .description(accountMission.getMission().getDescription())
                .goalCount(accountMission.getMission().getGoalCount())
                .progressCount(accountMission.getProgressCount())
                .stateCode(accountMission.getStateCode())
                .claimedAt(accountMission.getClaimedAt())
                .rewards(accountMission.getMission().getRewards().stream()
                        .map(reward -> GetMissionRewardResponseDto.builder()
                                .rewardTypeCode(reward.getRewardTypeCode())
                                .rewardCode(reward.getRewardCode())
                                .inventoryTypeCode(reward.getInventoryTypeCode())
                                .amount(reward.getAmount())
                                .build())
                        .toList())
                .build();
    }
}
