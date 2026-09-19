package com.monglife.mongs.application.member.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.member.port.errorCode.ApplicationMemberAdminErrorCode;
import lombok.Getter;

import java.util.Collections;

@Getter
public class InvalidAdminSlotCountException extends ErrorException {

    public InvalidAdminSlotCountException() {
        this.errorCode = ApplicationMemberAdminErrorCode.INVALID_ADMIN_SLOT_COUNT;
        this.result = Collections.emptyMap();
    }
}
