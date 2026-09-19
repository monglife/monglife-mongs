package com.monglife.mongs.application.mong.port.in.utils;

import com.monglife.mongs.domain.mission.enums.*;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mission.model.Mission;
import com.monglife.mongs.domain.mission.model.MissionReward;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;

import java.util.List;
import java.util.Set;

public class MissionTestUtil {

    /**
     * 테스트 미션 마스터 생성 (리워드 없음)
     */
    public static Mission getMission(Long missionId, MissionCycleCode cycleCode, MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, Integer goalCount) {
        return getMission(missionId, cycleCode, actionCode, goalTypeCode, goalCount, List.of());
    }

    public static Mission getMission(Long missionId, MissionCycleCode cycleCode, MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, Integer goalCount, List<MissionReward> rewards) {
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
                .rewards(rewards)
                .build();
    }

    public static AccountMission getAccountMission(Long accountMissionId, Long accountId, Mission mission, String cycleKey) {
        return getAccountMission(accountMissionId, accountId, mission, cycleKey, 0, MissionStateCode.IN_PROGRESS);
    }

    public static AccountMission getAccountMission(Long accountMissionId, Long accountId, Mission mission, String cycleKey, Integer progressCount, MissionStateCode stateCode) {
        return AccountMission.builder()
                .accountMissionId(accountMissionId)
                .accountId(accountId)
                .mission(mission)
                .cycleKey(cycleKey)
                .progressCount(progressCount)
                .stateCode(stateCode)
                .detailCodes(Set.of())
                .build();
    }

    public static MissionReward reward(MissionRewardTypeCode rewardTypeCode, Integer amount) {
        return MissionReward.builder()
                .missionRewardId(1L)
                .missionId(1L)
                .rewardTypeCode(rewardTypeCode)
                .amount(amount)
                .build();
    }

    public static MissionReward inventoryReward(String rewardCode, InventoryTypeCode inventoryTypeCode, Integer amount) {
        return MissionReward.builder()
                .missionRewardId(1L)
                .missionId(1L)
                .rewardTypeCode(MissionRewardTypeCode.INVENTORY)
                .rewardCode(rewardCode)
                .inventoryTypeCode(inventoryTypeCode)
                .amount(amount)
                .build();
    }
}
