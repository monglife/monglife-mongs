package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMongAdminErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class InvalidCreateMasterException extends ErrorException {

    public InvalidCreateMasterException() {
        this.errorCode = ApplicationMongAdminErrorCode.INVALID_CREATE_MASTER;
        this.result = Collections.emptyMap();
    }
}
