package com.monglife.mongs.domain.mission.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionStateCode {
    IN_PROGRESS("진행 중"),
    CLAIMABLE("수령 가능"),
    CLAIMED("수령 완료"),
    ;

    private final String description;
}
