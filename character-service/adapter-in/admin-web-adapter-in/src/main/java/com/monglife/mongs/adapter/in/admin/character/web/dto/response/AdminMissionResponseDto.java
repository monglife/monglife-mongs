package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.enums.MissionRewardTypeCode;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMissionVo;
import com.monglife.mongs.domain.mission.model.Mission;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
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

    /** 로테이션 그룹. 주간·월간은 주기마다 한 그룹씩 돌아가며 나간다 */
    private final Integer rotationGroup;

    /** 지금 사용자에게 나가는 중인가. true 면 수정·삭제가 막히고 노출 토글만 열린다 */
    private final Boolean isPublished;

    /** 이번 주기 구간 (서비스 기준 시간대). 주간은 월요일~일요일 */
    private final LocalDate periodStart;
    private final LocalDate periodEnd;

    /** 이번 주기에 당첨된 그룹. 이 값과 rotationGroup 이 같으면 게시 중이다 */
    private final Integer currentRotationGroup;

    private final List<Reward> rewards;

    @Getter
    @Builder
    public static class Reward {
        private final MissionRewardTypeCode rewardTypeCode;
        private final String rewardCode;
        private final InventoryTypeCode inventoryTypeCode;
        private final Integer amount;
    }

    public static AdminMissionResponseDto of(AdminMissionVo vo) {

        Mission mission = vo.getMission();

        return AdminMissionResponseDto.builder()
                .rotationGroup(mission.getRotationGroup())
                .isPublished(vo.getIsPublished())
                .periodStart(vo.getPeriodStart())
                .periodEnd(vo.getPeriodEnd())
                .currentRotationGroup(vo.getCurrentRotationGroup())
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
