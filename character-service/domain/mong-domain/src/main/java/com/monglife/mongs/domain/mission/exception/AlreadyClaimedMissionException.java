package com.monglife.mongs.domain.mission.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.domain.mission.errorCode.DomainMissionErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class AlreadyClaimedMissionException extends ErrorException {

    public AlreadyClaimedMissionException() {
        this.errorCode = DomainMissionErrorCode.ALREADY_CLAIMED_MISSION;
        this.result = Collections.emptyMap();
    }
}
