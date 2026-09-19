package com.monglife.mongs.application.mong.port.enums;

public interface SchedulerType {

    String getCode();

    Long getExpiration();

    /**
     * 주기 방식. 관리자 등록이 어떤 생성 경로를 탈지 이 값으로 고른다.
     *
     * <p>만료 초로 추론하면 안 된다 - 알 부화(300초)는 일회성이고 지수 감소(900초)는 반복이라
     * 초만 보고는 갈라지지 않는다.
     */
    ScheduleKind getKind();

    enum ScheduleKind {
        /** 한 번 발화하고 끝난다. 알 부화·사망 */
        ONCE,
        /** 만료 초마다 반복. 지수 증감·배변 */
        CYCLE,
        /** 매일 정해진 시각에 반복. 수면·기상 */
        FIXED_TIME_CYCLE,
    }
}
