package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMissionErrorCode;
import lombok.Getter;

import java.util.Collections;

/**
 * (액션, 목표 타입, 목표치) 가 기존 미션과 겹칠 때.
 *
 * <p>유니크 키 {@code uk_mission_goal} 이 그 세 컬럼이라 이대로 두면 DB 가 터지고 원시 500 이 나간다.
 * 주기는 키에 없으므로 같은 주기든 다른 주기든 걸린다.
 *
 * <p>{@link DuplicatedMissionGoalException} 과 다르다. 그쪽은 "(액션, 목표 타입) 쌍이 다른 주기에
 * 이미 있다" 는 겹침 방지 규칙이고, 이쪽은 목표치까지 똑같은 경우다.
 */
@Getter
public class DuplicatedMissionGoalCountException extends ErrorException {

    public DuplicatedMissionGoalCountException() {
        this.errorCode = ApplicationMissionErrorCode.DUPLICATED_MISSION_GOAL_COUNT;
        this.result = Collections.emptyMap();
    }
}
