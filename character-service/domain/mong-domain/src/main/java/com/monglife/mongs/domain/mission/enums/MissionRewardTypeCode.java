package com.monglife.mongs.domain.mission.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionRewardTypeCode {
    /** 몽 경험치. maxStatus 로 잘린다 */
    EXP("경험치"),
    /** 몽 페이 포인트 */
    PAY_POINT("페이 포인트"),
    /** 플레이어 스타 포인트. user-service 소유라 Kafka 로 넘긴다 */
    STAR_POINT("스타 포인트"),
    /** 몽 인벤토리 아이템 */
    INVENTORY("인벤토리 아이템"),
    ;

    private final String description;
}
