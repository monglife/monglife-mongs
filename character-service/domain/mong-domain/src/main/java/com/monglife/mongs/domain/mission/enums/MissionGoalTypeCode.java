package com.monglife.mongs.domain.mission.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 미션 목표 타입.
 *
 * <p>일간/주간/월간이 서로 겹치지 않게 하는 축이다. 같은 액션이라도 목표 타입이 다르면 다른 미션이다 -
 * 일간 "밥 5번 주기"(COUNT)와 주간 "서로 다른 음식 5종 먹이기"(DISTINCT)는 겹치지 않는다.
 * 반대로 일간 "밥 5번"과 주간 "밥 20번"은 같은 (액션, 타입) 쌍이라 등록이 거부된다.
 */
@Getter
@AllArgsConstructor
public enum MissionGoalTypeCode {
    /** 액션 실행 횟수. 같은 대상을 반복해도 계속 오른다 */
    COUNT("실행 횟수"),
    /** 서로 다른 대상의 개수. 같은 대상을 반복하면 오르지 않는다 (음식 종류, 아이템 종류, 날짜 ...) */
    DISTINCT("서로 다른 대상 수"),
    /** 액션이 발생시킨 수치의 누적 (소비 페이 포인트, 획득 경험치 ...) */
    ACCUMULATE("수치 누적"),
    ;

    private final String description;
}
