package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMissionErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class NotExistsMissionException extends ErrorException {

    public NotExistsMissionException() {
        this.errorCode = ApplicationMissionErrorCode.NOT_EXISTS_MISSION;
        this.result = Collections.emptyMap();
    }
}
