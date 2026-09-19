package com.monglife.mongs.adapter.in.mong.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.enums.MissionStateCode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GetMissionResponseDto {

    /** 리워드 수령 시 넘기는 값 */
    private Long accountMissionId;

    private String missionCode;

    private MissionCycleCode cycleCode;

    private MissionGoalTypeCode goalTypeCode;

    private String title;

    private String description;

    private Integer goalCount;

    private Integer progressCount;

    private MissionStateCode stateCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
    private LocalDateTime claimedAt;

    private List<GetMissionRewardResponseDto> rewards;

    @Builder
    public GetMissionResponseDto(Long accountMissionId, String missionCode, MissionCycleCode cycleCode, MissionGoalTypeCode goalTypeCode, String title, String description, Integer goalCount, Integer progressCount, MissionStateCode stateCode, LocalDateTime claimedAt, List<GetMissionRewardResponseDto> rewards) {
        this.accountMissionId = accountMissionId;
        this.missionCode = missionCode;
        this.cycleCode = cycleCode;
        this.goalTypeCode = goalTypeCode;
        this.title = title;
        this.description = description;
        this.goalCount = goalCount;
        this.progressCount = progressCount;
        this.stateCode = stateCode;
        this.claimedAt = claimedAt;
        this.rewards = rewards;
    }
}
