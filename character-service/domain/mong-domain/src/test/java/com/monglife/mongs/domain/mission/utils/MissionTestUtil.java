package com.monglife.mongs.domain.mission.utils;

import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.enums.MissionRewardTypeCode;
import com.monglife.mongs.domain.mission.enums.MissionStateCode;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mission.model.Mission;
import com.monglife.mongs.domain.mission.model.MissionReward;

import java.util.List;
import java.util.Set;

public class MissionTestUtil {

    /**
     * 테스트 미션 마스터 생성
     */
    public static Mission getMission(Long missionId, MissionCycleCode cycleCode, MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, Integer goalCount) {
        return Mission.builder()
                .missionId(missionId)
                .missionCode("MS_TEST_" + missionId)
                .cycleCode(cycleCode)
                .actionCode(actionCode)
                .goalTypeCode(goalTypeCode)
                .title("TEST-MISSION-TITLE")
                .description("TEST-MISSION-DESCRIPTION")
                .goalCount(goalCount)
                .isActive(true)
                .sortOrder(missionId.intValue())
                .rewards(List.of(MissionReward.builder()
                        .missionRewardId(missionId)
                        .missionId(missionId)
                        .rewardTypeCode(MissionRewardTypeCode.PAY_POINT)
                        .amount(100)
                        .build()))
                .build();
    }

    /**
     * 진행 중인 테스트 사용자 미션 생성
     */
    public static AccountMission getAccountMission(Long accountId, Mission mission) {
        return getAccountMission(accountId, mission, 0, MissionStateCode.IN_PROGRESS, Set.of());
    }

    public static AccountMission getAccountMission(Long accountId, Mission mission, Integer progressCount, MissionStateCode stateCode, Set<String> detailCodes) {
        return AccountMission.builder()
                .accountMissionId(mission.getMissionId())
                .accountId(accountId)
                .mission(mission)
                .cycleKey("20260917")
                .progressCount(progressCount)
                .stateCode(stateCode)
                .detailCodes(detailCodes)
                .build();
    }
}
