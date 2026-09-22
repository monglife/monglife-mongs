package com.monglife.mongs.application.mong.port.in.admin.command;

import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.model.MissionReward;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 미션 등록.
 *
 * <p>다른 마스터처럼 {@code AdminCreateMasterCommand} 의 kind 로 받지 않는다 -
 * 미션은 리워드가 중첩 목록이라 평면 union 커맨드로는 표현되지 않고,
 * 공통 코드도 만들지 않아 그쪽 등록 경로의 공통 처리가 하나도 맞지 않는다.
 */
@Getter
public class AdminCreateMissionCommand {

    private final String missionCode;

    private final MissionCycleCode cycleCode;

    private final MissionActionCode actionCode;

    private final MissionGoalTypeCode goalTypeCode;

    private final String title;

    private final String description;

    private final Integer goalCount;

    private final Boolean isActive;

    private final Integer sortOrder;

    /** 로테이션 그룹. 주간·월간만 의미가 있다 */
    private final Integer rotationGroup;

    private final List<MissionReward> rewards;

    @Builder
    public AdminCreateMissionCommand(String missionCode, MissionCycleCode cycleCode, MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, String title, String description, Integer goalCount, Boolean isActive, Integer sortOrder, Integer rotationGroup, List<MissionReward> rewards) {
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
