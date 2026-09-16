package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMongAdminErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class AlreadyExistsMasterCodeException extends ErrorException {

    public AlreadyExistsMasterCodeException() {
        this.errorCode = ApplicationMongAdminErrorCode.ALREADY_EXISTS_MASTER_CODE;
        this.result = Collections.emptyMap();
    }
}
