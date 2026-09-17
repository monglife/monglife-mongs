package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.enums.MissionRewardTypeCode;
import com.monglife.mongs.domain.mission.model.Mission;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminMissionResponseDto {

    private final Long missionId;
    private final String missionCode;
    private final MissionCycleCode cycleCode;
    private final MissionActionCode actionCode;
    private final MissionGoalTypeCode goalTypeCode;
    private final String title;
    private final String description;
    private final Integer goalCount;
    private final Boolean isActive;
    private final Integer sortOrder;
    private final List<Reward> rewards;

    @Getter
    @Builder
    public static class Reward {
        private final MissionRewardTypeCode rewardTypeCode;
        private final String rewardCode;
        private final InventoryTypeCode inventoryTypeCode;
        private final Integer amount;
    }

    public static AdminMissionResponseDto of(Mission mission) {
        return AdminMissionResponseDto.builder()
                .missionId(mission.getMissionId())
                .missionCode(mission.getMissionCode())
                .cycleCode(mission.getCycleCode())
                .actionCode(mission.getActionCode())
                .goalTypeCode(mission.getGoalTypeCode())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .goalCount(mission.getGoalCount())
                .isActive(mission.getIsActive())
                .sortOrder(mission.getSortOrder())
                .rewards(mission.getRewards().stream()
                        .map(reward -> Reward.builder()
                                .rewardTypeCode(reward.getRewardTypeCode())
                                .rewardCode(reward.getRewardCode())
                                .inventoryTypeCode(reward.getInventoryTypeCode())
                                .amount(reward.getAmount())
                                .build())
                        .toList())
                .build();
    }
}
