package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMissionErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class DuplicatedMissionGoalException extends ErrorException {

    public DuplicatedMissionGoalException() {
        this.errorCode = ApplicationMissionErrorCode.DUPLICATED_MISSION_GOAL;
        this.result = Collections.emptyMap();
    }
}
