package com.monglife.mongs.domain.battle.errorCode;

import com.monglife.core.enums.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DomainBattleErrorCode implements ErrorCode {

    ALREADY_ENTER_MATCH_PLAYER("500-100-000", "이미 입장한 매치 플레이어 입니다."),
    ALREADY_EXIT_MATCH_PLAYER("500-100-001", "이미 퇴장한 매치 플레이어 입니다."),
    ALREADY_START_MATCH("500-100-002", "이미 시작한 매치 입니다."),
    ALREADY_EXISTS_MATCH_PICK("500-100-003", "이미 현재 라운드에 선택을 완료했습니다."),
    NOT_EXISTS_MATCH_PLAYER("500-100-004", "매치 플레이어가 존재하지 않습니다."),
    NOT_PICKED_ALL_MATCH_PLAYERS("500-100-005", "모든 매치 플레이어가 매치 선택을 하지 않았습니다"),
    NOT_ENTERING_MATCH("500-100-006", "입장 대기 중인 매치가 아닙니다."),
    ALREADY_END_MATCH("500-100-007", "이미 종료된 매치 입니다."),
    ;

    private final String code;

    private final String message;
}
