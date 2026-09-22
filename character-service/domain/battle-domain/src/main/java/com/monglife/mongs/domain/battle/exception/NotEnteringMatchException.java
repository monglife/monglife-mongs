package com.monglife.mongs.domain.battle.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.domain.battle.errorCode.DomainBattleErrorCode;
import lombok.Getter;

import java.util.Collections;

/**
 * 입장 대기 중이 아닌 매치를 취소하려 할 때.
 *
 * <p>입장 기한 스위퍼가 후보를 고른 뒤 잠그기 전에, 마지막 플레이어의 입장 이벤트가 먼저
 * 도착해 매치가 시작될 수 있다. 그 경합에서 진 스위퍼가 살아 있는 매치를 닫고 참가비까지
 * 돌려주는 일을 막는 마지막 방어선이다.
 */
@Getter
public class NotEnteringMatchException extends ErrorException {

    public NotEnteringMatchException() {
        this.errorCode = DomainBattleErrorCode.NOT_ENTERING_MATCH;
        this.result = Collections.emptyMap();
    }
}
