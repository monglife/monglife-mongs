package com.monglife.mongs.application.mong.port.in.admin.command;

import com.monglife.mongs.domain.mission.model.MissionReward;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 미션 수정.
 *
 * <p>정체성에 해당하는 네 값은 바꾸지 않는다 - {@code missionCode}, {@code cycleCode},
 * {@code actionCode}, {@code goalTypeCode}.
 *
 * <ul>
 *   <li>{@code missionCode} 는 유니크 키이고 운영 로그·시드 SQL 이 이 값으로 미션을 가리킨다.</li>
 *   <li>{@code cycleCode}·{@code actionCode}·{@code goalTypeCode} 를 바꾸면 이미 적재된
 *       사용자 미션의 진행도가 다른 의미의 숫자가 된다. "밥 5번"을 "간식 5번"으로 바꾸면
 *       밥으로 쌓은 3이 간식 3으로 둔갑한다.</li>
 * </ul>
 *
 * <p>그런 변경이 필요하면 새 미션을 등록하고 옛 미션을 비활성으로 내린다.
 *
 * <p>{@code goalCount} 는 바꿀 수 있다. 다만 유니크 키 {@code uk_mission_goal}
 * (액션, 목표 타입, 목표치) 에 걸리므로 서비스가 중복을 먼저 본다.
 */
@Getter
public class AdminUpdateMissionCommand {

    private final Long missionId;

    private final String title;

    private final String description;

    private final Integer goalCount;

    private final Boolean isActive;

    private final Integer sortOrder;

    /** 로테이션 그룹. 주간·월간만 의미가 있다 */
    private final Integer rotationGroup;

    /** 통째로 교체한다. 부분 수정이 아니다 */
    private final List<MissionReward> rewards;

    @Builder
    public AdminUpdateMissionCommand(Long missionId, String title, String description, Integer goalCount, Boolean isActive, Integer sortOrder, Integer rotationGroup, List<MissionReward> rewards) {
        this.missionId = missionId;
        this.title = title;
        this.description = description;
        this.goalCount = goalCount;
        this.isActive = isActive;
        this.sortOrder = sortOrder;
        this.rotationGroup = rotationGroup == null ? 0 : rotationGroup;
        this.rewards = rewards == null ? Collections.emptyList() : rewards;
    }
}
