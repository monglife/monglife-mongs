package com.monglife.mongs.application.member.port.errorCode;

import com.monglife.core.enums.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationMemberErrorCode implements ErrorCode {

    NOT_EXISTS_PLAYER("400-201-000", "플레이어가 존재하지 않습니다."),
    NOT_EXISTS_EXCHANGE_STAR_POINT_PRODUCT("400-201-001", "스타 포인트 환전 상품이 존재하지 않습니다."),
    NOT_EXISTS_ORDER("400-201-002", "주문 내역이 존재하지 않습니다."),
    NOT_EXISTS_IN_APP_PRODUCT("400-201-003", "인앱 상품이 존재하지 않습니다."),
    NOT_EXISTS_IN_APP_ORDER("400-201-004", "인앱 상품 주문 내역이 존재하지 않습니다."),
    INVALID_CONSUME_IN_APP_ORDER("400-201-005", "인앱 상품 주문 소비를 할 수 없습니다."),
    INVALID_CREATE_COLLECTION_MAP("400-201-006", "컬렉션 맵을 등록하는데 실패했습니다."),
    INVALID_CREATE_COLLECTION_MONG("400-201-007", "컬렉션 몽을 등록하는데 실패했습니다."),
    INVALID_CREATE_ORDER("400-201-008", "주문을 등록하는데 실패했습니다."),
    INVALID_CREATE_PLAYER("400-201-009", "플레이어를 등록하는데 실패했습니다."),
    // 400-201-010 은 오류 신고(INVALID_CREATE_FEEDBACK) 자리였다. 신고가 discovery common-api 로
    // 옮겨지면서 제거했다. 코드는 와이어 값이라 뒤 번호를 당기지 않고 자리를 비워 둔다.
    NOT_EXISTS_NOTICE("400-201-011", "공지 사항 조회에 실패했습니다.."),
    ;

    private final String code;

    private final String message;
}
