package com.monglife.mongs.application.mong.port.errorCode;

import com.monglife.core.enums.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationMongAdminErrorCode implements ErrorCode {

    NOT_EXISTS_TASK("400-101-100", "몽 스케줄이 존재하지 않습니다."),
    ;

    private final String code;

    private final String message;
}
