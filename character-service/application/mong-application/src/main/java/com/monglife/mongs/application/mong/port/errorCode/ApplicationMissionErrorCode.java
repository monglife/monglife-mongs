package com.monglife.mongs.application.mong.port.errorCode;

import com.monglife.core.enums.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 미션 유스케이스 에러 코드. 컨텍스트 번호 102 는 미션 도메인과 같이 쓴다.
 */
@Getter
@AllArgsConstructor
public enum ApplicationMissionErrorCode implements ErrorCode {

    NOT_EXISTS_MISSION("400-102-000", "미션이 존재하지 않습니다."),
    INVALID_CREATE_ACCOUNT_MISSION("400-102-001", "미션 적재에 실패했습니다."),
    ALREADY_EXISTS_MISSION_CODE("400-102-002", "이미 등록된 미션 코드입니다."),
    DUPLICATED_MISSION_GOAL("400-102-003", "같은 액션·목표 타입이 다른 주기에 이미 등록되어 있습니다."),
    MISSION_IN_USE("400-102-004", "사용자가 진행 중인 미션은 삭제할 수 없습니다."),
    ;

    private final String code;

    private final String message;
}
