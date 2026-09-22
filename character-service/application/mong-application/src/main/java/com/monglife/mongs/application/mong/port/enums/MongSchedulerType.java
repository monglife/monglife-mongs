package com.monglife.mongs.application.mong.port.enums;

import com.monglife.mongs.application.mong.port.exception.InvalidSchedulerTypeCodeException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 몽 스케줄 타입.
 *
 * <p>⚠ enum 이름과 {@link #getCode()} 가 다르다 (7종 중 4종). <b>정본은 코드 쪽이다</b> -
 * {@code mongs_task.scheduler_type_code} 에 저장되고 조회 쿼리도 이 값으로 맞춘다.
 * 이름을 저장하거나 이름으로 조회하는 곳은 없다.
 */
@Getter
@AllArgsConstructor
public enum MongSchedulerType implements SchedulerType {

    EGG_EVOLUTION("EGG-EVOLUTION", 300L, ScheduleKind.ONCE),
    SLEEP("SLEEP", 86400L, ScheduleKind.FIXED_TIME_CYCLE),
    WAKEUP("WAKEUP", 86400L, ScheduleKind.FIXED_TIME_CYCLE),
    INCREASE_STATUS("INCREASE-STATUS", 900L, ScheduleKind.CYCLE),
    DECREASE_STATUS("DECREASE-STATUS", 900L, ScheduleKind.CYCLE),
    INCREASE_POOP("INCREASE-POOP", 3600L, ScheduleKind.CYCLE),
    DEAD("DEAD", 43200L, ScheduleKind.ONCE),
    ;

    private final String code;

    private final Long expiration;

    private final ScheduleKind kind;

    /**
     * 문자열 → 타입. <b>코드와 enum 이름을 둘 다 받는다.</b>
     *
     * <p>코드가 정본이지만 이름도 받는 이유는 배포 순서 때문이다. 관리자 웹과 백엔드가 따로
     * 배포되는데, 관리자 웹은 그동안 이름(DECREASE_STATUS)을 보내 왔다. 코드만 받게 하면
     * 백엔드가 먼저 나가는 순간 아직 안 바뀐 관리자 웹의 스케줄 등록이 통째로 막힌다.
     *
     * <p>둘 다 받아도 모호하지 않다 - 7종의 코드와 이름을 통틀어 겹치는 값이 없다.
     *
     * <p>Jackson 어노테이션을 쓰지 않는다. 이 모듈에는 Jackson 이 없고(application 계층 전체가
     * 그렇다), 직렬화 프레임워크를 여기까지 끌어들일 이유도 없다. 파싱은 어댑터가 한다.
     */
    public static MongSchedulerType fromCode(String value) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(value) || type.name().equals(value))
                .findFirst()
                .orElseThrow(InvalidSchedulerTypeCodeException::new);
    }
}
