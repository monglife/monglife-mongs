package com.monglife.mongs.application.mong.port.errorCode;

import com.monglife.core.enums.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationMongAdminErrorCode implements ErrorCode {

    NOT_EXISTS_TASK("400-101-100", "몽 스케줄이 존재하지 않습니다."),
    ALREADY_EXISTS_MASTER_CODE("400-101-101", "이미 등록된 코드입니다."),
    NOT_EXISTS_MASTER_CODE("400-101-102", "등록되지 않은 공통 코드입니다."),
    INVALID_CREATE_MASTER("400-101-103", "마스터 데이터 등록에 실패했습니다."),
    ;

    private final String code;

    private final String message;
}
