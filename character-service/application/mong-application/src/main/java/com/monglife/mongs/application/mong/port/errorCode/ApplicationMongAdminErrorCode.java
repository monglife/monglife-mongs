package com.monglife.mongs.application.mong.port.errorCode;

import com.monglife.core.enums.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationMongAdminErrorCode implements ErrorCode {

    NOT_EXISTS_TASK("400-101-100", "몽 스케줄이 존재하지 않습니다."),
    ALREADY_EXISTS_TASK("400-101-101", "같은 타입의 스케줄이 이미 등록되어 있습니다. 지운 뒤 다시 등록하세요."),
    NOT_EXISTS_MASTER_CODE("400-101-102", "등록되지 않은 공통 코드입니다."),
    INVALID_CREATE_MASTER("400-101-103", "마스터 데이터 등록에 실패했습니다."),
    NOT_EXISTS_MASTER("400-101-104", "마스터 데이터가 존재하지 않습니다."),
    // 400-101-101 을 ALREADY_EXISTS_TASK 와 함께 쓰고 있었다. 클라이언트가 코드로 분기할 수 없어 갈랐다.
    // 태스크 쪽을 그대로 둔 이유: 관리자 웹이 화면에 노출하는 게 그 코드이고,
    // user-service 의 같은 이름(ApplicationMemberAdminErrorCode)도 이미 끝자리가 102 다.
    ALREADY_EXISTS_MASTER_CODE("400-101-105", "이미 등록된 코드입니다."),
    /** 스케줄 타입 코드를 알아볼 수 없을 때. 코드(DECREASE-STATUS)와 enum 이름(DECREASE_STATUS) 둘 다 받는다 */
    INVALID_SCHEDULER_TYPE_CODE("400-101-106", "알 수 없는 스케줄 타입 코드입니다."),
    ;

    private final String code;

    private final String message;
}
