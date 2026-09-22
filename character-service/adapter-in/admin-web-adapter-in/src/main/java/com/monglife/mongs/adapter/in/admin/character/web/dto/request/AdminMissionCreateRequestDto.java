package com.monglife.mongs.adapter.in.admin.character.web.dto.request;

import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
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
 * 미션 등록.
 *
 * <p>겹침 방지 규칙이 있다 - 같은 {@code (actionCode, goalTypeCode)} 는 한 주기에서만 쓸 수 있다.
 * 같은 주기 안에서 {@code goalCount} 만 다른 미션을 여러 개 두는 것은 정상이다(일간 후보를 늘리는 방법).
 */
@Getter
@Setter
@NoArgsConstructor
public class AdminMissionCreateRequestDto {

    @NotBlank
    @Size(max = 32)
    private String missionCode;

    @NotNull
    private MissionCycleCode cycleCode;

    @NotNull
    private MissionActionCode actionCode;

    @NotNull
    private MissionGoalTypeCode goalTypeCode;

    @NotBlank
    @Size(max = 64)
    private String title;

    @Size(max = 128)
    private String description;

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
