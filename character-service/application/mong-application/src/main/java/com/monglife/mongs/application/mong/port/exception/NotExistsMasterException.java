package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMongAdminErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class NotExistsMasterException extends ErrorException {

    public NotExistsMasterException() {
        this.errorCode = ApplicationMongAdminErrorCode.NOT_EXISTS_MASTER;
        this.result = Collections.emptyMap();
    }
}
