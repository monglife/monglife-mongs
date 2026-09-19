package com.monglife.mongs.domain.mission.errorCode;

import com.monglife.core.enums.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 미션 도메인 에러 코드.
 *
 * <p>코드 체계는 {@code 5(도메인)00-{컨텍스트}-{순번}} 이다. character-service 는
 * 100=battle, 101=mong 을 쓰고 있어 미션은 102 를 새로 잡는다.
 */
@Getter
@AllArgsConstructor
public enum DomainMissionErrorCode implements ErrorCode {

    FORBIDDEN_MISSION("500-102-000", "미션에 대한 권한이 없습니다."),
    NOT_CLAIMABLE_MISSION("500-102-001", "아직 달성하지 못한 미션입니다."),
    ALREADY_CLAIMED_MISSION("500-102-002", "이미 리워드를 수령한 미션입니다."),
    INVALID_MISSION_REWARD("500-102-003", "지급할 수 없는 리워드입니다."),
    ;

    private final String code;

    private final String message;
}
