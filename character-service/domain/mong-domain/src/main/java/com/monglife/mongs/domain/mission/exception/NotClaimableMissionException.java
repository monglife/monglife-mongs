package com.monglife.mongs.domain.mission.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.domain.mission.errorCode.DomainMissionErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class NotClaimableMissionException extends ErrorException {

    public NotClaimableMissionException() {
        this.errorCode = DomainMissionErrorCode.NOT_CLAIMABLE_MISSION;
        this.result = Collections.emptyMap();
    }
}
