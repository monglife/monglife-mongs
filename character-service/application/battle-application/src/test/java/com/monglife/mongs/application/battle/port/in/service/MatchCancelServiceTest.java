package com.monglife.mongs.application.battle.port.in.service;

import com.monglife.core.utils.CommonUtil;
import com.monglife.mongs.application.battle.port.in.MatchUseCase;
import com.monglife.mongs.application.battle.port.out.MatchPersistencePort;
import com.monglife.mongs.application.battle.port.out.MatchPublishPort;
import com.monglife.mongs.application.battle.port.out.MatchReadPort;
import com.monglife.mongs.application.battle.port.out.MongPersistencePort;
import com.monglife.mongs.domain.battle.enums.MatchStateCode;
import com.monglife.mongs.domain.battle.model.Match;
import com.monglife.mongs.domain.battle.model.MatchPlayer;
import com.monglife.mongs.domain.mong.model.Mong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 입장 기한 초과 매치 취소 + 참가비 환불 단위 테스트.
 */
class MatchCancelServiceTest {

    private final MongPersistencePort mongPersistencePort = Mockito.mock(MongPersistencePort.class);
    private final MatchPersistencePort matchPersistencePort = Mockito.mock(MatchPersistencePort.class);
    private final MatchReadPort matchReadPort = Mockito.mock(MatchReadPort.class);
    private final MatchPublishPort matchPublishPort = Mockito.mock(MatchPublishPort.class);
    private final MatchUseCase matchUseCase = new MatchService(mongPersistencePort, matchPersistencePort, matchReadPort, matchPublishPort);

    private static final Long MATCH_ID = 1L;
    private static final Long MONG_ID = 7L;
    private static final int START_PAY_POINT = 300;

    private static MatchPlayer human(Long mongId) {
        return MatchPlayer.builder()
                .playerId(CommonUtil.randomId()).deviceId(CommonUtil.randomId())
                .accountId(1L).mongId(mongId)
                .mongCode("CH100").mongName("별몽").name("사람")
                .attack(50D).heal(10D).defence(25D)
                .isBot(false).hp(100D).isEnter(false)
                .build();
    }

    private static MatchPlayer bot() {
        return MatchPlayer.builder()
                .playerId(CommonUtil.randomId()).deviceId(CommonUtil.randomId())
                .accountId(-1L).mongId(-1L)
                .mongCode("CH101").mongName("동글몽").name("동글몽 봇")
                .attack(50D).heal(10D).defence(25D)
                .isBot(true).hp(100D).isEnter(true).enteredAt(LocalDateTime.now())
                .build();
    }

    private static Mong mong(Long mongId) {
        return Mong.builder()
                .mongId(mongId).accountId(1L)
                .mongCode("CH100").mongName("별몽").name("테스트몽")
                .isSleep(false)
                .strength(100D).satiety(100D).healthy(100D).fatigue(100D)
                .exp(0D).weight(100D)
                .payPoint(START_PAY_POINT)
                .build();
    }

    private static Match canceledMatch(List<MatchPlayer> players) {
        return Match.builder()
                .matchId(MATCH_ID).round(0).maxRound(Match.getInitMaxRound())
                .stateCode(MatchStateCode.CANCELED)
                .matchPlayers(players)
                .build();
    }

    @Test
    @DisplayName("취소된 매치의 사람 참가자에게 참가비를 돌려주고 종료를 알린다.")
    void cancelEnteringMatchRefundsHuman() {
        // arrange
        Match match = canceledMatch(List.of(human(MONG_ID), bot()));
        Mong mong = mong(MONG_ID);

        Mockito.when(matchPersistencePort.cancelEnteringMatchPort(MATCH_ID)).thenReturn(Optional.of(match));
        Mockito.when(mongPersistencePort.getMongPort(MONG_ID)).thenReturn(Optional.of(mong));
        Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

        // act
        Optional<Match> result = matchUseCase.cancelEnteringMatchUseCase(MATCH_ID);

        // assert
        assertTrue(result.isPresent());
        assertEquals(START_PAY_POINT + Match.getBettingPayPoint(), mong.getPayPoint());
        Mockito.verify(matchPublishPort).publishMatchEndPort(match);
    }

    @Test
    @DisplayName("봇에게는 환불하지 않는다. 봇은 참가비를 내지 않았다.")
    void cancelEnteringMatchDoesNotRefundBot() {
        // arrange
        Match match = canceledMatch(List.of(human(MONG_ID), bot()));
        Mong mong = mong(MONG_ID);

        Mockito.when(matchPersistencePort.cancelEnteringMatchPort(MATCH_ID)).thenReturn(Optional.of(match));
        Mockito.when(mongPersistencePort.getMongPort(MONG_ID)).thenReturn(Optional.of(mong));
        Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

        // act
        matchUseCase.cancelEnteringMatchUseCase(MATCH_ID);

        // assert — 사람 1명분만 조회한다. 봇 몽 ID(-1) 로는 조회하지 않는다
        Mockito.verify(mongPersistencePort, Mockito.times(1)).getMongPort(Mockito.anyLong());
        Mockito.verify(mongPersistencePort, Mockito.never()).getMongPort(-1L);
    }

    @Test
    @DisplayName("사람 둘이면 둘 다 돌려준다.")
    void cancelEnteringMatchRefundsAllHumans() {
        // arrange
        Match match = canceledMatch(List.of(human(1L), human(2L)));
        Mong first = mong(1L);
        Mong second = mong(2L);

        Mockito.when(matchPersistencePort.cancelEnteringMatchPort(MATCH_ID)).thenReturn(Optional.of(match));
        Mockito.when(mongPersistencePort.getMongPort(1L)).thenReturn(Optional.of(first));
        Mockito.when(mongPersistencePort.getMongPort(2L)).thenReturn(Optional.of(second));
        Mockito.when(mongPersistencePort.saveMongPort(Mockito.any())).thenAnswer(it -> Optional.of(it.getArgument(0)));

        // act
        matchUseCase.cancelEnteringMatchUseCase(MATCH_ID);

        // assert
        assertEquals(START_PAY_POINT + Match.getBettingPayPoint(), first.getPayPoint());
        assertEquals(START_PAY_POINT + Match.getBettingPayPoint(), second.getPayPoint());
    }

    @Test
    @DisplayName("경합에서 지면(그 사이 매치가 시작되면) 아무것도 하지 않는다. 환불도 알림도 없다.")
    void cancelEnteringMatchWhenAlreadyStarted() {
        // arrange — 어댑터가 잠금 아래에서 ENTERING 이 아님을 확인하고 빈 값을 준다
        Mockito.when(matchPersistencePort.cancelEnteringMatchPort(MATCH_ID)).thenReturn(Optional.empty());

        // act
        Optional<Match> result = matchUseCase.cancelEnteringMatchUseCase(MATCH_ID);

        // assert
        assertTrue(result.isEmpty());
        Mockito.verify(mongPersistencePort, Mockito.never()).getMongPort(Mockito.anyLong());
        Mockito.verify(matchPublishPort, Mockito.never()).publishMatchEndPort(Mockito.any());
    }

    @Test
    @DisplayName("한 명의 환불이 실패해도 나머지 환불과 종료 알림은 진행된다.")
    void cancelEnteringMatchContinuesWhenOneRefundFails() {
        // arrange — 1번 몽은 사라졌고 2번은 살아 있다
        Match match = canceledMatch(List.of(human(1L), human(2L)));
        Mong second = mong(2L);

        Mockito.when(matchPersistencePort.cancelEnteringMatchPort(MATCH_ID)).thenReturn(Optional.of(match));
        Mockito.when(mongPersistencePort.getMongPort(1L)).thenReturn(Optional.empty());
        Mockito.when(mongPersistencePort.getMongPort(2L)).thenReturn(Optional.of(second));
        Mockito.when(mongPersistencePort.saveMongPort(second)).thenReturn(Optional.of(second));

        // act
        Optional<Match> result = matchUseCase.cancelEnteringMatchUseCase(MATCH_ID);

        // assert
        assertTrue(result.isPresent());
        assertEquals(START_PAY_POINT + Match.getBettingPayPoint(), second.getPayPoint());
        Mockito.verify(matchPublishPort).publishMatchEndPort(match);
    }
}
