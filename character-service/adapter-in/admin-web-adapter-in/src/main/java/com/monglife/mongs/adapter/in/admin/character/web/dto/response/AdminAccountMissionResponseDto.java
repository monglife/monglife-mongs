package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionStateCode;
import com.monglife.mongs.domain.mission.model.AccountMission;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminAccountMissionResponseDto {

    private final Long accountMissionId;
    private final Long accountId;
    private final String missionCode;
    private final MissionCycleCode cycleCode;
    private final String cycleKey;
    private final String title;
    private final Integer goalCount;
    private final Integer progressCount;
    private final MissionStateCode stateCode;

    /** DISTINCT 미션에서 지금까지 집계된 대상들. 문의 대응 때 어디까지 인정됐는지 보려면 필요하다 */
    private final List<String> detailCodes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
    private final LocalDateTime claimedAt;

    public static AdminAccountMissionResponseDto of(AccountMission accountMission) {
        return AdminAccountMissionResponseDto.builder()
                .accountMissionId(accountMission.getAccountMissionId())
                .accountId(accountMission.getAccountId())
                .missionCode(accountMission.getMission().getMissionCode())
                .cycleCode(accountMission.getMission().getCycleCode())
                .cycleKey(accountMission.getCycleKey())
                .title(accountMission.getMission().getTitle())
                .goalCount(accountMission.getMission().getGoalCount())
                .progressCount(accountMission.getProgressCount())
                .stateCode(accountMission.getStateCode())
                .detailCodes(accountMission.getDetailCodes().stream().sorted().toList())
                .claimedAt(accountMission.getClaimedAt())
                .build();
    }
}
