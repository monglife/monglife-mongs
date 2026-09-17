package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMissionErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class InvalidCreateAccountMissionException extends ErrorException {

    public InvalidCreateAccountMissionException() {
        this.errorCode = ApplicationMissionErrorCode.INVALID_CREATE_ACCOUNT_MISSION;
        this.result = Collections.emptyMap();
    }
}
