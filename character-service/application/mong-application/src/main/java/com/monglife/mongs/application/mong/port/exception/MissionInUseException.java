package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMissionErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class MissionInUseException extends ErrorException {

    public MissionInUseException() {
        this.errorCode = ApplicationMissionErrorCode.MISSION_IN_USE;
        this.result = Collections.emptyMap();
    }
}
