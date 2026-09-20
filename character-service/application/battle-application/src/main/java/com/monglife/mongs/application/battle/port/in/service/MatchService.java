package com.monglife.mongs.application.battle.port.in.service;

import com.monglife.mongs.application.battle.port.exception.NotEndMatchException;
import com.monglife.mongs.application.battle.port.exception.NotExistsMatchException;
import com.monglife.mongs.application.battle.port.exception.NotExistsMongException;
import com.monglife.mongs.application.battle.port.in.MatchUseCase;
import com.monglife.mongs.application.battle.port.in.command.*;
import com.monglife.mongs.application.battle.port.in.vo.MatchOutcomeVo;
import com.monglife.mongs.application.battle.port.out.MatchPersistencePort;
import com.monglife.mongs.application.battle.port.out.MatchPublishPort;
import com.monglife.mongs.application.battle.port.out.MatchReadPort;
import com.monglife.mongs.application.battle.port.out.MongPersistencePort;
import com.monglife.mongs.domain.battle.model.Match;
import com.monglife.mongs.domain.battle.model.MatchPick;
import com.monglife.mongs.domain.battle.model.MatchPlayer;
import com.monglife.mongs.domain.mong.model.Mong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService implements MatchUseCase {

    private final MongPersistencePort mongPersistencePort;

    private final MatchPersistencePort matchPersistencePort;

    private final MatchReadPort matchReadPort;

    private final MatchPublishPort matchPublishPort;

    /**
     * 매치 리워드, 차감 페이 포인트 조회
     */
    @Override
    public MatchOutcomeVo getMatchOutcomeUseCase() {
        return MatchOutcomeVo.builder()
                .exp(Match.getRewardExp())
                .rewardPayPoint(Match.getRewardPayPoint())
                .bettingPayPoint(Match.getBettingPayPoint())
                .build();
    }

    /**
     * 매치 조회
     */
    @Override
    @Transactional
    public Match getMatchUseCase(GetMatchCommand command) {
        return matchReadPort.getMatchPort(command.getMatchId())
                .orElseThrow(NotExistsMatchException::new);
    }

    /**
     * 승리 매치 플레이어 조회
     */
    @Override
    @Transactional
    public MatchPlayer getWinMatchPlayerUseCase(GetWinMatchPlayerCommand command) {

        Match match = matchReadPort.getMatchPort(command.getMatchId())
                .orElseThrow(NotExistsMatchException::new);

        // 매치 종료 여부 확인
        if (!match.isEnd()) {
            throw new NotEndMatchException();
        }

        return match.getWinner();
    }

    /**
     * 매치 입장
     */
    @Override
    @Transactional
    public Match enterMatchUseCase(EnterMatchCommand command) {

        Match match = matchPersistencePort.getMatchPort(command.getMatchId())
                .orElseThrow(NotExistsMatchException::new);

        // 취소된 매치도 막는다. isEnd() 만 보면 CANCELED 가 통과한다
        if (match.isTerminal()) {
            throw new NotExistsMatchException();
        }

        // 플레이어 입장
        match.enterMatchPlayer(command.getPlayerId());

        matchPersistencePort.saveMatchPort(match)
                .orElseThrow(NotExistsMatchException::new);

        // 매치가 시작된 경우 매치 정보 비동기 응답
        if (match.isStart()) {
            matchPublishPort.publishMatchStartPort(match);
        }

        return match;
    }

    /**
     * 매치 퇴장
     */
    @Override
    @Transactional
    public Match exitMatchUseCase(ExitMatchCommand command) {

        Match match = matchPersistencePort.getMatchPort(command.getMatchId())
                .orElseThrow(NotExistsMatchException::new);

        // isEnd() 로 두면 취소된 매치에 퇴장이 들어왔을 때 통과해 승리 보상이 지급된다
        if (!match.isTerminal()) {
            // 플레이어 퇴장
            match.exitMatchPlayer(command.getPlayerId());

            // 매치 정보 동기화
            matchPersistencePort.saveMatchPort(match)
                    .orElseThrow(NotExistsMatchException::new);

            // 매치가 중단된 경우
            if (match.isAllMatchPlayersExited()) {
                // 승리한 매치 플레이어 조회
                MatchPlayer winMatchPlayer = match.getWinner();

                if (!winMatchPlayer.getIsBot()) {
                    // 승리한 매치 플레이어 몽 조회
                    Mong mong = mongPersistencePort.getMongPort(winMatchPlayer.getMongId())
                            .orElseThrow(NotExistsMongException::new);

                    // 매치 승리 보상 적용
                    mong.matchReward(Match.getRewardPayPoint(), Match.getRewardExp());

                    // 몽 동기화
                    mongPersistencePort.saveMongPort(mong);
                }

                // 매치 종료 비동기 응답
                matchPublishPort.publishMatchEndPort(match);
            }
        }

        return match;
    }

    /**
     * 입장 기한 초과 매치 취소 + 참가비 환불.
     *
     * <p><b>일부러 {@code @Transactional} 을 걸지 않는다.</b> 매치 상태(battle)와 페이 포인트(mong)는
     * 데이터소스가 다르고, 둘을 묶는 {@code ChainedTransactionManager} 는 커밋을 순서대로 시도할 뿐
     * 원자성을 보장하지 않는다. 한 트랜잭션으로 묶으면 mong 만 커밋되고 battle 이 실패했을 때
     * 매치가 ENTERING 으로 남아 스위퍼가 <b>매 틱마다 다시 환불</b>한다.
     *
     * <p>그래서 순서를 고정한다 — 상태를 먼저 확정하고, 그 다음 환불한다. 최악의 경우가
     * "환불 누락 + 감사 로그" 이지 이중 환불이 아니다. 두 데이터소스에 걸쳐 정확히 한 번을
     * 보장하려면 아웃박스가 필요한데, 이 규모에 그건 과하다.
     */
    @Override
    public Optional<Match> cancelEnteringMatchUseCase(Long matchId) {

        // 1. 상태 확정 (battle). 잠금 아래에서 ENTERING 인지 다시 확인한다
        Optional<Match> canceled = matchPersistencePort.cancelEnteringMatchPort(matchId);

        if (canceled.isEmpty()) {
            return Optional.empty();
        }

        Match match = canceled.get();

        // 2. 참가비 환불 (mong). 건별로 독립이다 - 한 명이 실패해도 나머지는 돌려준다
        match.getMatchPlayers().stream()
                .filter(matchPlayer -> !matchPlayer.getIsBot())
                .forEach(this::refundBetting);

        // 3. 앱에 종료 알림. 환불이 반영된 뒤에 보내야 앱이 옛 잔액을 읽지 않는다
        matchPublishPort.publishMatchEndPort(match);

        return Optional.of(match);
    }

    /**
     * 배팅 환불 1건. 실패해도 다른 참가자의 환불과 매치 종료 알림을 막지 않는다.
     */
    private void refundBetting(MatchPlayer matchPlayer) {
        try {
            Mong mong = mongPersistencePort.getMongPort(matchPlayer.getMongId())
                    .orElseThrow(NotExistsMongException::new);

            mong.matchBettingCancel(Match.getBettingPayPoint());

            mongPersistencePort.saveMongPort(mong)
                    .orElseThrow(NotExistsMongException::new);

            log.info("매치 취소 - 참가비 환불 mongId={} payPoint={}", matchPlayer.getMongId(), Match.getBettingPayPoint());
        } catch (Exception exception) {
            // 몽이 지워졌거나 저장에 실패했다. 여기서 터뜨리면 남은 참가자도 못 돌려받고,
            // 매치는 이미 취소돼 있어 스위퍼가 다시 집어 주지도 않는다. 기록만 남긴다.
            log.error("매치 취소 - 참가비 환불 실패 mongId={} payPoint={}",
                    matchPlayer.getMongId(), Match.getBettingPayPoint(), exception);
        }
    }

    /**
     * 매치 라운드 선택
     */
    @Override
    @Transactional
    public Match pickMatchUseCase(PickMatchCommand command) {

        Match match = matchPersistencePort.getMatchPort(command.getMatchId())
                .orElseThrow(NotExistsMatchException::new);

        if (match.isTerminal()) {
            throw new NotExistsMatchException();
        }

        // 매치 플레이어 조회
        MatchPlayer matchPlayer = match.getMatchPlayer(command.getPlayerId());
        // 상대 매치 플레이어 조회
        MatchPlayer targetMatchPlayer = match.getMatchPlayer(command.getTargetPlayerId());

        // 매치 선택 도메인 객체 생성
        MatchPick matchPick = switch (command.getPickCode()) {
            case MATCH_PICK_DEFENCE -> MatchPick.builder()
                    .matchPlayer(matchPlayer)
                    .targetMatchPlayer(targetMatchPlayer)
                    .round(match.getCurrentRound())
                    .pickCode(command.getPickCode())
                    .pickValue(matchPlayer.getDefence())
                    .build();
            case MATCH_PICK_HEAL -> MatchPick.builder()
                    .matchPlayer(matchPlayer)
                    .targetMatchPlayer(targetMatchPlayer)
                    .round(match.getCurrentRound())
                    .pickCode(command.getPickCode())
                    .pickValue(matchPlayer.getHeal())
                    .build();
            case MATCH_PICK_ATTACK -> MatchPick.builder()
                    .matchPlayer(matchPlayer)
                    .targetMatchPlayer(targetMatchPlayer)
                    .round(match.getCurrentRound())
                    .pickCode(command.getPickCode())
                    .pickValue(matchPlayer.getAttack())
                    .build();
        };

        // 매치 선택 등록
        boolean isRoundOver = match.pickMatchPlayer(matchPick);

        // 매치 정보 동기화
        matchPersistencePort.saveMatchPort(match)
                .orElseThrow(NotExistsMatchException::new);

        // 다음 라운드 진행한 경우
        if (isRoundOver) {
            // 매치 라운드 종료 비동기 응답
            matchPublishPort.publishMatchPort(match);
        }

        if (match.isEnd()) {
            // 승리한 매치 플레이어 조회
            MatchPlayer winMatchPlayer = match.getWinner();

            if (!winMatchPlayer.getIsBot()) {
                // 승리한 매치 플레이어 몽 조회
                Mong mong = mongPersistencePort.getMongPort(winMatchPlayer.getMongId())
                        .orElseThrow(NotExistsMongException::new);

                // 매치 승리 보상 적용
                mong.matchReward(Match.getRewardPayPoint(), Match.getRewardExp());

                // 몽 동기화
                mongPersistencePort.saveMongPort(mong);
            }
        }

        return match;
    }
}
