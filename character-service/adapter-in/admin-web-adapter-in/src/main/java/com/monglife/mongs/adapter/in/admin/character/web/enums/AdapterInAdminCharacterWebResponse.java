package com.monglife.mongs.adapter.in.admin.character.web.enums;

import com.monglife.core.enums.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AdapterInAdminCharacterWebResponse implements Response {

    PING(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-000", "관리자 인증에 성공했습니다."),

    GET_MONGS(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-010", "몽 목록 조회에 성공했습니다."),
    GET_MONG(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-011", "몽 조회에 성공했습니다."),
    UPDATE_MONG_STATUS(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-012", "몽 지수 수정에 성공했습니다."),
    UPDATE_MONG_STATE(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-013", "몽 상태 변경에 성공했습니다."),
    UPDATE_MONG_SLEEP(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-014", "몽 수면·기상 전환에 성공했습니다."),
    DELETE_MONG(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-015", "몽 삭제에 성공했습니다."),
    GET_TASKS(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-016", "몽 스케줄 목록 조회에 성공했습니다."),
    PAUSE_TASK(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-017", "몽 스케줄 일시 중지에 성공했습니다."),
    RESUME_TASK(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-018", "몽 스케줄 재시작에 성공했습니다."),
    GET_EVOLUTION_HISTORIES(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-019", "몽 진화 이력 조회에 성공했습니다."),
    GET_INVENTORIES(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-020", "인벤토리 목록 조회에 성공했습니다."),

    GRANT_INVENTORY(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-021", "인벤토리 아이템 지급에 성공했습니다."),

    GET_STATS(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-022", "통계 조회에 성공했습니다."),

    GET_MONG_TYPES(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-030", "몽 타입 목록 조회에 성공했습니다."),
    GET_FOODS(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-031", "음식 목록 조회에 성공했습니다."),
    GET_SNACKS(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-032", "간식 목록 조회에 성공했습니다."),
    GET_TRAINING_TYPES(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-033", "훈련 타입 목록 조회에 성공했습니다."),
    GET_RANDOM_DRAWS(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-034", "랜덤 뽑기 아이템 목록 조회에 성공했습니다."),
    CREATE_MASTER(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-035", "마스터 데이터 등록에 성공했습니다."),

    GET_QUEUE_PLAYERS(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-040", "매치 대기열 조회에 성공했습니다."),
    DELETE_QUEUE_PLAYER(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-041", "매치 대기열 강제 이탈에 성공했습니다."),
    GET_MATCHES(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-042", "매치 목록 조회에 성공했습니다."),
    GET_MATCH(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-043", "매치 조회에 성공했습니다."),
    TERMINATE_MATCH(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-044", "매치 강제 종료에 성공했습니다."),
    GET_BATTLE_STATS(HttpStatus.OK.value(), "MONGS-CHARACTER-ADMIN-045", "배틀 통계 조회에 성공했습니다."),
    ;

    private final Integer httpStatus;

    private final String code;

    private final String message;
}
