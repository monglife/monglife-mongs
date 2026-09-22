package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMissionErrorCode;
import lombok.Getter;

import java.util.Collections;

/**
 * 게시 중인 미션을 수정·삭제하려 할 때.
 *
 * <p>노출을 내리면 게시가 풀려 수정할 수 있다. 다음 주기로 넘어가도 풀린다.
 */
@Getter
public class MissionPublishedException extends ErrorException {

    public MissionPublishedException() {
        this.errorCode = ApplicationMissionErrorCode.MISSION_PUBLISHED;
        this.result = Collections.emptyMap();
    }
}
