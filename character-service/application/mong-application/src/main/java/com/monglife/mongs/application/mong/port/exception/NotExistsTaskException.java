package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMongAdminErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class NotExistsTaskException extends ErrorException {

    public NotExistsTaskException() {
        this.errorCode = ApplicationMongAdminErrorCode.NOT_EXISTS_TASK;
        this.result = Collections.emptyMap();
    }
}
