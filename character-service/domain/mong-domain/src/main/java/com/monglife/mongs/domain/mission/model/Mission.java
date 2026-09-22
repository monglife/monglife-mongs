package com.monglife.mongs.domain.mission.model;

import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * 미션 마스터. 사용자와 무관한 정의다.
 */
@Getter
@ToString
public class Mission {

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

    /**
     * 로테이션 그룹.
     *
     * <p>주간·월간은 활성 미션을 그룹으로 나눠 주기마다 한 그룹씩 돌아가며 내보낸다.
     * 같은 그룹끼리 한 주기에 함께 나간다. 일간은 쓰지 않는다 - 사용자별 무작위 선정이라
     * 그룹으로 묶으면 뽑기 후보가 좁아진다.
     */
    private final Integer rotationGroup;

    private final List<MissionReward> rewards;

    @Builder
    public Mission(Long missionId, String missionCode, MissionCycleCode cycleCode, MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, String title, String description, Integer goalCount, Boolean isActive, Integer sortOrder, Integer rotationGroup, List<MissionReward> rewards) {
        this.missionId = missionId;
        this.missionCode = missionCode;
        this.cycleCode = cycleCode;
        this.actionCode = actionCode;
        this.goalTypeCode = goalTypeCode;
        this.title = title;
        this.description = description;
        this.goalCount = goalCount;
        this.isActive = isActive;
        this.sortOrder = sortOrder;
        this.rotationGroup = rotationGroup == null ? 0 : rotationGroup;
        this.rewards = rewards == null ? Collections.emptyList() : rewards;
    }
}
