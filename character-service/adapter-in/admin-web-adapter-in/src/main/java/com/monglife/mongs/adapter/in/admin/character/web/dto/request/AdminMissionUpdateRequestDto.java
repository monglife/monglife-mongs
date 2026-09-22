package com.monglife.mongs.adapter.in.admin.character.web.dto.request;

import com.monglife.mongs.domain.mission.enums.MissionRewardTypeCode;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 미션 수정.
 *
 * <p>{@code missionCode}·{@code cycleCode}·{@code actionCode}·{@code goalTypeCode} 는 받지 않는다.
 * 정체성이라 바꾸면 이미 적재된 사용자 미션의 진행도가 다른 의미의 숫자가 된다.
 * 그런 변경이 필요하면 새 미션을 등록하고 옛 미션을 비활성으로 내린다.
 *
 * <p>{@code rewards} 는 부분 수정이 아니라 통째 교체다. 빈 목록은 받지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
public class AdminMissionUpdateRequestDto {

    @NotBlank
    @Size(max = 64)
    private String title;

    @Size(max = 128)
    private String description;

    /** 유니크 키 uk_mission_goal 에 걸리므로 다른 미션과 겹치면 400-102-003 이다 */
    @NotNull
    @Min(1)
    private Integer goalCount;

    private Boolean isActive;

    private Integer sortOrder;

    /** 로테이션 그룹. 주간·월간만 의미가 있다. 비우면 0 */
    @Min(0)
    private Integer rotationGroup;

    @Valid
    @NotEmpty
    private List<Reward> rewards;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Reward {

        @NotNull
        private MissionRewardTypeCode rewardTypeCode;

        /** INVENTORY 일 때 필수. 지급할 음식·간식 공통 코드 */
        @Size(max = 32)
        private String rewardCode;

        /** INVENTORY 일 때 필수. MAP 은 몽 인벤토리가 아니라 받지 않는다 */
        private InventoryTypeCode inventoryTypeCode;

        @NotNull
        @Min(1)
        private Integer amount;
    }
}
