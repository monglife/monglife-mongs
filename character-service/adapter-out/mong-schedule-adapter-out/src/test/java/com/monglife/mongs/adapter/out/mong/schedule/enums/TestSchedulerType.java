package com.monglife.mongs.adapter.out.mong.schedule.enums;

import com.monglife.mongs.application.mong.port.enums.SchedulerType;

public enum TestSchedulerType implements SchedulerType {

    CREATE_TEST("TEST-SCHEDULER-CREATE-TYPE-CODE", 3L),

    DELETE_1_TEST("TEST-SCHEDULER-DELETE-TYPE-CODE 1", 5L),
    DELETE_2_TEST("TEST-SCHEDULER-DELETE-TYPE-CODE 2", 5L),

    APP_STOP_1_TEST("TEST-SCHEDULER-APP-STOP-TYPE-CODE 1", 5L),
    APP_STOP_2_TEST("TEST-SCHEDULER-APP-STOP-TYPE-CODE 2", 5L),
    APP_STOP_3_TEST("TEST-SCHEDULER-APP-STOP-TYPE-CODE 3", 5L),
    APP_STOP_4_TEST("TEST-SCHEDULER-APP-STOP-TYPE-CODE 4", 5L),
    APP_STOP_5_TEST("TEST-SCHEDULER-APP-STOP-TYPE-CODE 5", 5L),
    ;

    private final String code;

    private final Long expiration;

    TestSchedulerType(String code, Long expiration) {
        this.code = code;
        this.expiration = expiration;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public Long getExpiration() {
        return this.expiration;
    }

    /** 테스트는 전부 시간 반복으로 본다. 이 열거형을 쓰는 곳이 관리자 등록 경로가 아니다 */
    @Override
    public ScheduleKind getKind() {
        return ScheduleKind.CYCLE;
    }
}
