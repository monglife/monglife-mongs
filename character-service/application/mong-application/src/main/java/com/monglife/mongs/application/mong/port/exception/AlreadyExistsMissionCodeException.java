package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMissionErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class AlreadyExistsMissionCodeException extends ErrorException {

    public AlreadyExistsMissionCodeException() {
        this.errorCode = ApplicationMissionErrorCode.ALREADY_EXISTS_MISSION_CODE;
        this.result = Collections.emptyMap();
    }
}
