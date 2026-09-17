package com.monglife.mongs.application.member.port.errorCode;

import com.monglife.core.enums.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationMemberAdminErrorCode implements ErrorCode {

    INVALID_ADMIN_SLOT_COUNT("400-201-100", "슬롯 수는 1 이상 최대 슬롯 수 이하여야 합니다."),
    INVALID_ADMIN_CREATE_NOTICE("400-201-101", "공지 사항을 등록하는데 실패했습니다."),
    ALREADY_EXISTS_MASTER_CODE("400-201-102", "이미 등록된 코드입니다."),
    NOT_EXISTS_MASTER("400-201-103", "마스터 데이터가 존재하지 않습니다."),
    ;

    private final String code;

    private final String message;
}
