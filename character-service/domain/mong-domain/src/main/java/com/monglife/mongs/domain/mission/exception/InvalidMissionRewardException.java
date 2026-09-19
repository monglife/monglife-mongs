package com.monglife.mongs.domain.mission.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.domain.mission.errorCode.DomainMissionErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class InvalidMissionRewardException extends ErrorException {

    public InvalidMissionRewardException() {
        this.errorCode = DomainMissionErrorCode.INVALID_MISSION_REWARD;
        this.result = Collections.emptyMap();
    }
}
