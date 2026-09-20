package com.monglife.mongs.adapter.in.member.web.enums;

import com.monglife.core.enums.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AdapterInMemberWebResponse implements Response {

    GET_COLLECTION_MAPS(HttpStatus.OK.value(), "MONGS-USER-MEMBER-000", "컬렉션 맵 목록 조회에 성공했습니다."),
    GET_COLLECTION_MONGS(HttpStatus.OK.value(), "MONGS-USER-MEMBER-001", "컬렉션 몽 목록 조회에 성공했습니다."),
    SEARCH_COLLECTION_MAP(HttpStatus.OK.value(), "MONGS-USER-MEMBER-002", "컬렉션 맵 탐색에 성공했습니다."),

    // MONGS-USER-MEMBER-003 은 오류 신고(CREATE_FEEDBACK) 자리였다. 신고가 discovery common-api 로
    // 옮겨지면서 제거했다. 코드는 와이어 값이라 뒤 번호를 당기지 않고 자리를 비워 둔다.

    CREATE_PLAYER(HttpStatus.OK.value(), "MONGS-USER-MEMBER-004", "플레이어 등록에 성공했습니다."),
    GET_PLAYER(HttpStatus.OK.value(), "MONGS-USER-MEMBER-005", "플레이어 조회에 성공했습니다."),
    BUY_SLOT(HttpStatus.OK.value(), "MONGS-USER-MEMBER-006", "추가 슬롯 구매에 성공했습니다."),
    EXCHANGE_STAR_POINT(HttpStatus.OK.value(), "MONGS-USER-MEMBER-007", "스타 포인트 환전에 성공했습니다."),

    GET_IN_APP_PRODUCTS(HttpStatus.OK.value(), "MONGS-USER-MEMBER-008", "구글 인앱 상품 조회에 성공했습니다."),
    CONSUME_ORDER(HttpStatus.OK.value(), "MONGS-USER-MEMBER-009", "주문 소비에 성공했습니다."),
    GET_CONSUMED_ORDERS(HttpStatus.OK.value(), "MONGS-USER-MEMBER-010", "소비된 주문 목록 조회에 성공했습니다."),

    GET_NOTICE(HttpStatus.OK.value(), "MONGS-USER-MEMBER-011", "공지 사항 조회에 성공했습니다."),
    GET_NOTICES(HttpStatus.OK.value(), "MONGS-USER-MEMBER-012", "공지 사항 목록 조회에 성공했습니다."),
    ;

    private final Integer httpStatus;

    private final String code;

    private final String message;
}
