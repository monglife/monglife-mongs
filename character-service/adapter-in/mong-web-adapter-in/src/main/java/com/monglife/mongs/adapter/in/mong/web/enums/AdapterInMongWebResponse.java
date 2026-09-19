package com.monglife.mongs.adapter.in.mong.web.enums;

import com.monglife.core.enums.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AdapterInMongWebResponse implements Response {

    GET_TRAINING_TYPES(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-000", "훈련 타입 목록 조회에 성공했습니다."),
    GET_TRAINING_TYPE(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-001", "훈련 타입 조회에 성공헀습니다."),
    TRAINING_END(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-002", "훈련 완료 처리에 성공했습니다."),

    GET_FOODS(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-003", "음식 목록 조회에 성공했습니다."),
    GET_SNACKS(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-004", "간식 목록 조회에 성공했습니다."),
    FEED_FOOD(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-005", "음식 섭취에 성공했습니다."),
    FEED_SNACK(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-006", "간식 섭취에 성공했습니다."),
    GET_INVENTORIES(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-007", "인벤토리 목록 조회에 성공했습니다."),
    USE_INVENTORY(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-008", "인벤토리 아이템 소비에 성공했습니다."),
    BUY_RANDOM_DRAW_TICKET(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-009", "랜덤 뽑기 티켓 구매에 성공했습니다."),
    RANDOM_DRAW(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-010", "랜덤 뽑기에 성공했습니다."),

    GET_MONGS(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-011", "몽 목록 조회에 성공했습니다."),
    GET_MONG(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-012", "몽 조회에 성공했습니다."),
    CREATE_MONG(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-013", "몽 생성에 성공했습니다."),
    DELETE_MONG(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-014", "몽 삭제에 성공했습니다."),
    STROKE_MONG(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-015", "몽 쓰다듬기에 성공했습니다."),
    SLEEP_WAKEUP_MONG(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-016", "몽 수면/기상 처리에 성공했습니다."),
    POOP_CLEAN_MONG(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-017", "몽 배변처리에 성공했습니다."),
    EVOLUTION_MONG(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-018", "몽 진화에 성공했습니다."),
    GRADUATE_MONG(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-019", "몽 졸업에 성공했습니다."),

    GET_MISSIONS(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-020", "미션 목록 조회에 성공했습니다."),
    CLAIM_MISSION_REWARD(HttpStatus.OK.value(), "MONGS-CHARACTER-MONG-021", "미션 리워드 수령에 성공했습니다."),
    ;

    private final Integer httpStatus;

    private final String code;

    private final String message;
}
