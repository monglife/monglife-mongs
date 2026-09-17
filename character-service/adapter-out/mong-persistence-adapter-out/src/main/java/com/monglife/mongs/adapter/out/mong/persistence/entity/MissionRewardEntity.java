package com.monglife.mongs.adapter.out.mong.persistence.entity;

import com.monglife.mongs.domain.mission.enums.MissionRewardTypeCode;
import com.monglife.mongs.domain.mission.model.MissionReward;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mongs_mission_reward")
public class MissionRewardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_reward_id")
    private Long missionRewardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private MissionEntity mission;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type_code", length = 16, nullable = false)
    private MissionRewardTypeCode rewardTypeCode;

    /** INVENTORY 일 때 지급할 아이템 공통 코드. 그 외에는 null */
    @Column(name = "reward_code", length = 32)
    private String rewardCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_type_code", length = 16)
    private InventoryTypeCode inventoryTypeCode;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Builder
    public MissionRewardEntity(Long missionRewardId, MissionEntity mission, MissionRewardTypeCode rewardTypeCode, String rewardCode, InventoryTypeCode inventoryTypeCode, Integer amount) {
        this.missionRewardId = missionRewardId;
        this.mission = mission;
        this.rewardTypeCode = rewardTypeCode;
        this.rewardCode = rewardCode;
        this.inventoryTypeCode = inventoryTypeCode;
        this.amount = amount;
    }

    public MissionReward toDomain() {
        return MissionReward.builder()
                .missionRewardId(this.missionRewardId)
                .missionId(this.mission.getMissionId())
                .rewardTypeCode(this.rewardTypeCode)
                .rewardCode(this.rewardCode)
                .inventoryTypeCode(this.inventoryTypeCode)
                .amount(this.amount)
                .build();
    }
}
