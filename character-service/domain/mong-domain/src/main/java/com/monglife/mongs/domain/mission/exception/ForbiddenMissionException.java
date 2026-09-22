package com.monglife.mongs.domain.mission.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.domain.mission.errorCode.DomainMissionErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class ForbiddenMissionException extends ErrorException {

    public ForbiddenMissionException() {
        this.errorCode = DomainMissionErrorCode.FORBIDDEN_MISSION;
        this.result = Collections.emptyMap();
    }
}
