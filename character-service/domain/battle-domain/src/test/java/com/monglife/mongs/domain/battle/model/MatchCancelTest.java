package com.monglife.mongs.domain.battle.model;

import com.monglife.core.utils.CommonUtil;
import com.monglife.mongs.domain.battle.enums.MatchStateCode;
import com.monglife.mongs.domain.battle.exception.AlreadyEndMatchException;
import com.monglife.mongs.domain.battle.exception.NotEnteringMatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 입장 기한 초과 취소 단위 테스트.
 *
 * <p>기존 {@code MatchPlayerTestUtil} 을 쓰지 않는다 - 거기서 만드는 매치는 {@code stateCode} 가
 * null 이라 상태 전이를 다룰 수가 없다.
 */
class MatchCancelTest {

    private static final Long MATCH_ID = 1L;
    private static final Double HP = 100D;

    private static MatchPlayer human(boolean entered) {
        return MatchPlayer.builder()
                .playerId(CommonUtil.randomId())
                .deviceId(CommonUtil.randomId())
                .accountId(1L)
                .mongId(1L)
                .mongCode("CH100")
                .mongName("별몽")
                .name("사람")
                .attack(50D).heal(10D).defence(25D)
                .isBot(false)
                .hp(HP)
                .isEnter(entered)
                .build();
    }

    private static MatchPlayer bot() {
        return MatchPlayer.builder()
                .playerId(CommonUtil.randomId())
                .deviceId(CommonUtil.randomId())
                .accountId(-1L)
                .mongId(-1L)
                .mongCode("CH101")
                .mongName("동글몽")
                .name("동글몽 봇")
                .attack(50D).heal(10D).defence(25D)
                .isBot(true)
                .hp(HP)
                .isEnter(true)
                .enteredAt(java.time.LocalDateTime.now())
                .build();
    }

    private static Match match(MatchStateCode stateCode, List<MatchPlayer> players) {
        return Match.builder()
                .matchId(MATCH_ID)
                .round(0)
                .maxRound(Match.getInitMaxRound())
                .stateCode(stateCode)
                .matchPlayers(players)
                .build();
    }

    @Nested
    @DisplayName("입장 기한 초과 취소")
    class CancelEntering {

        @Test
        @DisplayName("입장 대기 중인 매치는 취소 상태가 된다.")
        void cancelEntering() {
            // arrange
            Match match = match(MatchStateCode.ENTERING, List.of(human(false), bot()));

            // act
            match.cancelEntering();

            // assert
            assertEquals(MatchStateCode.CANCELED, match.getStateCode());
            assertFalse(match.isEntering());
        }

        @Test
        @DisplayName("이미 시작한 매치는 취소할 수 없다. 스위퍼가 후보를 고른 뒤 입장이 도착한 경우다.")
        void cancelEnteringWhenAlreadyStarted() {
            // arrange
            Match match = match(MatchStateCode.PROCESS, List.of(human(true), bot()));

            // act & assert
            assertThrows(NotEnteringMatchException.class, match::cancelEntering);
            assertEquals(MatchStateCode.PROCESS, match.getStateCode());
        }

        @Test
        @DisplayName("이미 끝난 매치는 취소할 수 없다.")
        void cancelEnteringWhenAlreadyEnded() {
            // arrange
            Match match = match(MatchStateCode.END, List.of(human(true), bot()));

            // act & assert
            assertThrows(NotEnteringMatchException.class, match::cancelEntering);
        }

        @Test
        @DisplayName("두 번 취소할 수 없다.")
        void cancelEnteringTwice() {
            // arrange
            Match match = match(MatchStateCode.ENTERING, List.of(human(false), bot()));
            match.cancelEntering();

            // act & assert
            assertThrows(NotEnteringMatchException.class, match::cancelEntering);
        }
    }

    /**
     * {@link #cancelEntering()} 가드의 짝. 그쪽이 "살아 있는 매치를 취소하지 마라" 라면
     * 이쪽은 "끝난 매치의 마감 사유를 덮어쓰지 마라" 다.
     */
    @Nested
    @DisplayName("관리자 강제 종료 가드")
    class AdminEndGuard {

        @Test
        @DisplayName("진행 중인 매치는 END 로 마감된다.")
        void adminEndWhenProcess() {
            // arrange
            Match match = match(MatchStateCode.PROCESS, List.of(human(true), bot()));

            // act
            match.adminEnd();

            // assert
            assertEquals(MatchStateCode.END, match.getStateCode());
        }

        @Test
        @DisplayName("취소된 매치를 강제 종료하면 던진다. CANCELED 가 END 로 덮이면 취소 매치를 구분할 수 없다.")
        void adminEndWhenCanceled() {
            // arrange
            Match match = match(MatchStateCode.ENTERING, List.of(human(false), bot()));
            match.cancelEntering();

            // act & assert
            assertThrows(AlreadyEndMatchException.class, match::adminEnd);
            assertEquals(MatchStateCode.CANCELED, match.getStateCode());
        }

        @Test
        @DisplayName("두 번 강제 종료할 수 없다.")
        void adminEndTwice() {
            // arrange
            Match match = match(MatchStateCode.PROCESS, List.of(human(true), bot()));
            match.adminEnd();

            // act & assert
            assertThrows(AlreadyEndMatchException.class, match::adminEnd);
            assertEquals(MatchStateCode.END, match.getStateCode());
        }

        @Test
        @DisplayName("입장 대기 중인 매치는 막지 않는다. 그 경로는 호출부가 cancelEntering 으로 먼저 거른다.")
        void adminEndWhenEnteringIsNotGuarded() {
            // arrange
            Match match = match(MatchStateCode.ENTERING, List.of(human(false), bot()));

            // act & assert
            assertDoesNotThrow(match::adminEnd);
        }
    }

    @Nested
    @DisplayName("취소된 매치의 상태 판정")
    class CanceledMatchState {

        @Test
        @DisplayName("취소는 종료(isEnd)가 아니다. 승자 조회와 보상 지급이 이 값으로 갈린다.")
        void canceledIsNotEnd() {
            // arrange
            Match match = match(MatchStateCode.CANCELED, List.of(human(false), bot()));

            // assert
            assertFalse(match.isEnd());
        }

        @Test
        @DisplayName("취소도 진행 불가(isTerminal)다. 입장·선택·퇴장이 막혀야 한다.")
        void canceledIsTerminal() {
            // arrange
            Match match = match(MatchStateCode.CANCELED, List.of(human(false), bot()));

            // assert
            assertTrue(match.isTerminal());
        }

        @Test
        @DisplayName("취소도 마지막 라운드로 본다. 앱이 더 기다리지 않게 한다.")
        void canceledIsLastRound() {
            // arrange
            Match match = match(MatchStateCode.CANCELED, List.of(human(false), bot()));

            // assert
            assertTrue(match.isLastRound());
        }

        @Test
        @DisplayName("진행 중인 매치는 진행 불가가 아니다.")
        void processIsNotTerminal() {
            // arrange
            Match match = match(MatchStateCode.PROCESS, List.of(human(true), bot()));

            // assert
            assertFalse(match.isTerminal());
        }
    }

    @Nested
    @DisplayName("승자 조회 방어")
    class GetWinner {

        @Test
        @DisplayName("사람 둘이 모두 입장하지 않았고 체력이 같아도 터지지 않는다. exitedAt 이 둘 다 null 이다.")
        void getWinnerWhenBothNeverEntered() {
            // arrange
            Match match = match(MatchStateCode.CANCELED, List.of(human(false), human(false)));

            // act & assert
            assertDoesNotThrow(match::getWinner);
            assertNotNull(match.getWinner());
        }
    }
}
