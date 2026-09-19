package com.monglife.mongs.domain.mission.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 미션이 추적하는 사용자 행동.
 *
 * <p>여기에 상수를 추가하고 대상 UseCase 에 {@code @MissionProgress} 를 붙이면 새 액션이 생긴다.
 * 미션 자체는 마스터 테이블 행이라 시드/관리자 API 로 늘린다.
 *
 * <p>배틀과 걸음 수는 아직 없다. battle-application 은 mong-application 에 의존하지 않고,
 * 걸음 수는 user-service 라 Kafka 토픽이 따로 필요하다.
 */
@Getter
@AllArgsConstructor
public enum MissionActionCode {

    // --- 상호작용 (일간 COUNT 축 / 주간 DISTINCT 축) ---
    FEED_FOOD("음식 주기"),
    FEED_SNACK("간식 주기"),
    STROKE("쓰다듬기"),
    POOP_CLEAN("배변 치우기"),
    TRAINING_END("훈련 완료"),
    RANDOM_DRAW("랜덤 뽑기"),
    BUY_RANDOM_DRAW_TICKET("뽑기 티켓 구매"),
    USE_INVENTORY("인벤토리 아이템 사용"),
    SLEEP("몽 재우기"),
    WAKEUP("몽 깨우기"),

    // --- 이정표 (월간 COUNT 축) ---
    EVOLUTION("몽 진화"),
    GRADUATE("몽 졸업"),
    CREATE_MONG("몽 생성"),

    // --- 누적 수치 (주간/월간 ACCUMULATE 축) ---
    PAY_POINT_SPEND("페이 포인트 소비"),
    PAY_POINT_EARN("페이 포인트 획득"),
    EXP_EARN("경험치 획득"),
    TRAINING_SCORE("훈련 점수 획득"),

    /**
     * 몽을 돌본 날.
     *
     * <p>이 액션에는 훅이 붙지 않는다. 다른 어떤 액션이든 진행도가 들어오면
     * 미션 서비스가 오늘 날짜를 detail 로 해서 함께 올린다.
     */
    CARE_DAY("몽 돌본 날"),
    ;

    private final String description;
}
