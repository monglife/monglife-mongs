package com.monglife.mongs.domain.mission.model;

import com.monglife.mongs.domain.mission.enums.MissionRewardTypeCode;
import com.monglife.mongs.domain.mission.exception.InvalidMissionRewardException;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MissionReward {

    private final Long missionRewardId;

    private final Long missionId;

    private final MissionRewardTypeCode rewardTypeCode;

    /** INVENTORY 일 때 지급할 아이템 공통 코드(FD000 등). 그 외에는 null */
    private final String rewardCode;

    /** INVENTORY 일 때 아이템 종류. 그 외에는 null */
    private final InventoryTypeCode inventoryTypeCode;

    /** EXP/PAY_POINT/STAR_POINT 는 수량, INVENTORY 는 지급 개수 */
    private final Integer amount;

    @Builder
    public MissionReward(Long missionRewardId, Long missionId, MissionRewardTypeCode rewardTypeCode, String rewardCode, InventoryTypeCode inventoryTypeCode, Integer amount) {
        this.missionRewardId = missionRewardId;
        this.missionId = missionId;
        this.rewardTypeCode = rewardTypeCode;
        this.rewardCode = rewardCode;
        this.inventoryTypeCode = inventoryTypeCode;
        this.amount = amount;
    }

    /**
     * 지급 직전 검증.
     *
     * <p>생성자가 아니라 지급 시점에 본다. DB 에 이미 들어 있는 행을 읽다가 터지면
     * 미션 목록 조회 전체가 죽어 잘못된 행 하나가 기능을 통째로 막는다.
     *
     * @return 리워드 도메인 객체
     */
    public MissionReward verify() {

        if (this.amount == null || this.amount <= 0) {
            throw new InvalidMissionRewardException();
        }

        if (MissionRewardTypeCode.INVENTORY.equals(this.rewardTypeCode)) {
            // MAP 은 몽 인벤토리가 아니라 계정 도감이다. 여기로 들어오면 인벤에 쓸 수 없는
            // 행이 생겨 useInventory 에서 소비 불가 아이템으로 남는다.
            if (this.rewardCode == null || this.inventoryTypeCode == null
                    || InventoryTypeCode.MAP.equals(this.inventoryTypeCode)) {
                throw new InvalidMissionRewardException();
            }
        }

        return this;
    }
}
