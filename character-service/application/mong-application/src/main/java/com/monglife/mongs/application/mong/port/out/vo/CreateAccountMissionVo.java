package com.monglife.mongs.application.mong.port.out.vo;

import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateAccountMissionVo {

    private final Long accountId;

    private final Long missionId;

    private final MissionCycleCode cycleCode;

    private final String cycleKey;

    @Builder
    public CreateAccountMissionVo(Long accountId, Long missionId, MissionCycleCode cycleCode, String cycleKey) {
        this.accountId = accountId;
        this.missionId = missionId;
        this.cycleCode = cycleCode;
        this.cycleKey = cycleKey;
    }
}
