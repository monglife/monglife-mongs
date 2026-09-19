package com.monglife.mongs.application.mong.port.in;

import com.monglife.mongs.application.mong.port.in.command.ClaimMissionRewardCommand;
import com.monglife.mongs.application.mong.port.in.command.GetMissionsCommand;
import com.monglife.mongs.application.mong.port.in.command.IncreaseMissionProgressCommand;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mong.model.Mong;

import java.util.List;

public interface MissionUseCase {

    /**
     * 미션 목록 조회. 이번 주기 미션이 아직 없으면 적재하고 돌려준다
     */
    List<AccountMission> getMissionsUseCase(GetMissionsCommand command);

    /**
     * 미션 진행도 반영. 플레이 동작 훅에서 호출된다
     */
    void increaseMissionProgressUseCase(IncreaseMissionProgressCommand command);

    /**
     * 미션 리워드 수령
     */
    Mong claimMissionRewardUseCase(ClaimMissionRewardCommand command);
}
