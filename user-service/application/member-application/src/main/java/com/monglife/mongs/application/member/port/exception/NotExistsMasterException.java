package com.monglife.mongs.application.member.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.member.port.errorCode.ApplicationMemberAdminErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class NotExistsMasterException extends ErrorException {

    public NotExistsMasterException() {
        this.errorCode = ApplicationMemberAdminErrorCode.NOT_EXISTS_MASTER;
        this.result = Collections.emptyMap();
    }
}
