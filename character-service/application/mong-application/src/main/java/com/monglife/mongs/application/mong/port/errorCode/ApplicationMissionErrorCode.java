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
    MISSION_PUBLISHED("400-102-005", "게시 중인 미션은 수정·삭제할 수 없습니다. 노출을 내리거나 다음 주기를 기다리세요."),
    // 003 과 나눈 이유: 003 의 메시지는 "다른 주기에" 라 같은 주기 안의 목표치 충돌에는 거짓말이 된다.
    // 운영자가 받아야 할 안내도 다르다 - 003 은 주기를 옮기라는 뜻이고, 006 은 목표치를 바꾸라는 뜻이다.
    DUPLICATED_MISSION_GOAL_COUNT("400-102-006", "같은 액션·목표 타입·목표치의 미션이 이미 있습니다. 목표치를 다르게 하세요."),
    ;

    private final String code;

    private final String message;
}
