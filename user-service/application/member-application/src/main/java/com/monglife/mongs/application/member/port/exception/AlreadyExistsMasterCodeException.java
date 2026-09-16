package com.monglife.mongs.application.member.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.member.port.errorCode.ApplicationMemberAdminErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class AlreadyExistsMasterCodeException extends ErrorException {

    public AlreadyExistsMasterCodeException() {
        this.errorCode = ApplicationMemberAdminErrorCode.ALREADY_EXISTS_MASTER_CODE;
        this.result = Collections.emptyMap();
    }
}
