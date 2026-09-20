package com.monglife.mongs.application.battle.port.in;

import com.monglife.mongs.application.battle.port.in.command.*;
import com.monglife.mongs.application.battle.port.in.vo.MatchOutcomeVo;
import com.monglife.mongs.domain.battle.model.Match;
import com.monglife.mongs.domain.battle.model.MatchPlayer;

import java.util.Optional;

public interface MatchUseCase {

    MatchOutcomeVo getMatchOutcomeUseCase();

    Match getMatchUseCase(GetMatchCommand command);

    MatchPlayer getWinMatchPlayerUseCase(GetWinMatchPlayerCommand command);

    Match enterMatchUseCase(EnterMatchCommand command);

    Match exitMatchUseCase(ExitMatchCommand command);

    Match pickMatchUseCase(PickMatchCommand command);

    /**
     * 입장 기한 초과 매치 취소 + 참가비 환불.
     *
     * <p>입장 기한 스위퍼와 관리자 강제 종료가 함께 쓴다. 정산 규칙을 한 곳에 둔다 -
     * <b>입장 대기 중이던 매치만</b> 배팅을 돌려준다. 이미 시작한 매치는 치러진 경기라
     * 돌려주지 않는다.
     *
     * @param matchId 매치 ID
     * @return 취소된 매치. 이미 시작했거나 없으면 빈 값
     */
    Optional<Match> cancelEnteringMatchUseCase(Long matchId);
}
