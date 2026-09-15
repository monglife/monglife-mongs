package com.monglife.mongs.application.member.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.member.port.errorCode.ApplicationMemberAdminErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class InvalidAdminCreateNoticeException extends ErrorException {

    public InvalidAdminCreateNoticeException() {
        this.errorCode = ApplicationMemberAdminErrorCode.INVALID_ADMIN_CREATE_NOTICE;
        this.result = Collections.emptyMap();
    }
}
