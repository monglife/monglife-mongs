package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMongAdminErrorCode;
import lombok.Getter;

import java.util.Collections;

/**
 * 같은 타입의 스케줄이 이미 걸려 있을 때.
 *
 * <p>등록 경로가 기존 행을 재사용하는데, 그 행이 일시중지 상태면 expiredAt 이 null 이라
 * 타이머를 걸다 NPE 로 트랜잭션이 통째로 깨진다. 그 앞에서 막는다.
 */
@Getter
public class AlreadyExistsTaskException extends ErrorException {

    public AlreadyExistsTaskException() {
        this.errorCode = ApplicationMongAdminErrorCode.ALREADY_EXISTS_TASK;
        this.result = Collections.emptyMap();
    }
}
