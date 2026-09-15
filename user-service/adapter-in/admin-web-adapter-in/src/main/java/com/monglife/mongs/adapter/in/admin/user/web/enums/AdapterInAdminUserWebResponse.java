package com.monglife.mongs.adapter.in.admin.user.web.enums;

import com.monglife.core.enums.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AdapterInAdminUserWebResponse implements Response {

    PING(HttpStatus.OK.value(), "MONGS-USER-ADMIN-000", "관리자 인증에 성공했습니다."),

    GET_NOTICES(HttpStatus.OK.value(), "MONGS-USER-ADMIN-010", "공지 사항 목록 조회에 성공했습니다."),
    GET_NOTICE(HttpStatus.OK.value(), "MONGS-USER-ADMIN-011", "공지 사항 조회에 성공했습니다."),
    CREATE_NOTICE(HttpStatus.OK.value(), "MONGS-USER-ADMIN-012", "공지 사항 등록에 성공했습니다."),
    UPDATE_NOTICE(HttpStatus.OK.value(), "MONGS-USER-ADMIN-013", "공지 사항 수정에 성공했습니다."),
    HIDE_NOTICE(HttpStatus.OK.value(), "MONGS-USER-ADMIN-014", "공지 사항 숨김 여부 수정에 성공했습니다."),
    DELETE_NOTICE(HttpStatus.OK.value(), "MONGS-USER-ADMIN-015", "공지 사항 삭제에 성공했습니다."),

    GET_MEMBERS(HttpStatus.OK.value(), "MONGS-USER-ADMIN-020", "멤버 목록 조회에 성공했습니다."),
    GET_MEMBER(HttpStatus.OK.value(), "MONGS-USER-ADMIN-021", "멤버 조회에 성공했습니다."),
    ADJUST_STAR_POINT(HttpStatus.OK.value(), "MONGS-USER-ADMIN-022", "스타 포인트 가감에 성공했습니다."),
    UPDATE_SLOT_COUNT(HttpStatus.OK.value(), "MONGS-USER-ADMIN-023", "슬롯 수 수정에 성공했습니다."),
    GET_COLLECTION_MAPS(HttpStatus.OK.value(), "MONGS-USER-ADMIN-024", "컬렉션 맵 목록 조회에 성공했습니다."),
    GET_COLLECTION_MONGS(HttpStatus.OK.value(), "MONGS-USER-ADMIN-025", "컬렉션 몽 목록 조회에 성공했습니다."),

    GRANT_COLLECTION_MAP(HttpStatus.OK.value(), "MONGS-USER-ADMIN-026", "컬렉션 맵 지급에 성공했습니다."),
    GRANT_COLLECTION_MONG(HttpStatus.OK.value(), "MONGS-USER-ADMIN-027", "컬렉션 몽 지급에 성공했습니다."),

    GET_ORDERS(HttpStatus.OK.value(), "MONGS-USER-ADMIN-030", "주문 목록 조회에 성공했습니다."),
    GET_ORDER(HttpStatus.OK.value(), "MONGS-USER-ADMIN-031", "주문 조회에 성공했습니다."),
    RECONSUME_ORDER(HttpStatus.OK.value(), "MONGS-USER-ADMIN-032", "주문 재소비에 성공했습니다."),

    GET_STATS(HttpStatus.OK.value(), "MONGS-USER-ADMIN-040", "통계 조회에 성공했습니다."),

    GET_STEP(HttpStatus.OK.value(), "MONGS-USER-ADMIN-045", "걸음 수 환전 현황 조회에 성공했습니다."),
    RESET_STEP(HttpStatus.OK.value(), "MONGS-USER-ADMIN-046", "걸음 수 환전 상한 초기화에 성공했습니다."),

    GET_MAP_TYPES(HttpStatus.OK.value(), "MONGS-USER-ADMIN-050", "맵 타입 목록 조회에 성공했습니다."),
    GET_EXCHANGE_STAR_POINT_PRODUCTS(HttpStatus.OK.value(), "MONGS-USER-ADMIN-051", "스타 포인트 환전 상품 목록 조회에 성공했습니다."),
    ;

    private final Integer httpStatus;

    private final String code;

    private final String message;
}
