package com.monglife.mongs.domain.battle.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.domain.battle.errorCode.DomainBattleErrorCode;
import lombok.Getter;

import java.util.Collections;

/**
 * 이미 끝난(END·CANCELED) 매치를 다시 강제 종료하려 할 때.
 *
 * <p>{@link NotEnteringMatchException} 의 짝이다. 그쪽이 "살아 있는 매치를 취소하지 마라" 라면
 * 이쪽은 "끝난 매치의 마감 사유를 덮어쓰지 마라" 다.
 *
 * <p>관리자 목록은 30초 주기로 다시 읽고 입장 기한도 30초라, 스위퍼가 방금 CANCELED 로 마감한
 * 매치의 '강제 종료' 버튼이 화면에 남아 있는 구간이 늘 있다. 그 버튼을 누르면 CANCELED 가
 * END + round 0 으로 덮여, 취소 매치를 구분하려고 상태를 새로 만든 의미가 사라진다.
 * 돈이 어긋나지는 않는다 - 환불은 이미 끝났고 두 번 나가지 않는다. 사라지는 건 기록이다.
 */
@Getter
public class AlreadyEndMatchException extends ErrorException {

    public AlreadyEndMatchException() {
        this.errorCode = DomainBattleErrorCode.ALREADY_END_MATCH;
        this.result = Collections.emptyMap();
    }
}
