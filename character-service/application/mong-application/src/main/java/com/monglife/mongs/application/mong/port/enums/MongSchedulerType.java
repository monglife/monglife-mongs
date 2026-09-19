package com.monglife.mongs.application.mong.port.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
