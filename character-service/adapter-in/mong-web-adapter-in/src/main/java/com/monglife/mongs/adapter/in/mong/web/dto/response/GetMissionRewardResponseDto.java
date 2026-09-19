package com.monglife.mongs.adapter.in.mong.web.dto.response;

import com.monglife.mongs.domain.mission.enums.MissionRewardTypeCode;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GetMissionRewardResponseDto {

    private MissionRewardTypeCode rewardTypeCode;

    /** INVENTORY 일 때만 채워진다 */
    private String rewardCode;

    /** INVENTORY 일 때만 채워진다 */
    private InventoryTypeCode inventoryTypeCode;

    private Integer amount;

    @Builder
    public GetMissionRewardResponseDto(MissionRewardTypeCode rewardTypeCode, String rewardCode, InventoryTypeCode inventoryTypeCode, Integer amount) {
        this.rewardTypeCode = rewardTypeCode;
        this.rewardCode = rewardCode;
        this.inventoryTypeCode = inventoryTypeCode;
        this.amount = amount;
    }
}
