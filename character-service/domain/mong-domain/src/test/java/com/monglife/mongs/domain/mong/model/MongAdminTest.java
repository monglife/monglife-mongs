package com.monglife.mongs.domain.mong.model;

import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class MongAdminTest {

    private static final double MAX_STATUS = 100D;

    private Mong mong(MongStateCode stateCode) {
        return Mong.builder()
                .mongId(1L)
                .accountId(1L)
                .name("몽")
                .mongCode("CH100")
                .mongName("몽")
                .statusCode(MongStatusCode.NORMAL)
                .stateCode(stateCode)
                .level(1)
                .maxStatus(MAX_STATUS)
                .sleepAt(LocalTime.of(22, 0))
                .wakeupAt(LocalTime.of(8, 0))
                .payPoint(0)
                .isSleep(false)
                .strength(50D)
                .satiety(50D)
                .healthy(50D)
                .fatigue(50D)
                .exp(0D)
                .weight(10D)
                .evolutionReward(0D)
                .evolutionPenalty(0D)
                .strokeCount(0)
                .trainingCount(0)
                .poopCount(0)
                .randomDrawTicketCount(0)
                .build();
    }

    @Nested
    @DisplayName("관리자 지수 수정 단위 테스트")
    class AdminUpdateStatus {

        @Test
        @DisplayName("null 인 항목은 건드리지 않는다.")
        void keepsNullFields() {
            // arrange
            final Mong mong = mong(MongStateCode.NORMAL);

            // act
            mong.adminUpdateStatus(null, 70D, null, null, null, null, null, null, null);

            // assert
            assertEquals(70D, mong.getStrength());
            assertEquals(50D, mong.getSatiety());
            assertEquals(10D, mong.getWeight());
        }

        @Test
        @DisplayName("최대 지수를 넘는 값은 잘라 넣는다.")
        void clampsToMaxStatus() {
            // arrange
            final Mong mong = mong(MongStateCode.NORMAL);

            // act
            mong.adminUpdateStatus(null, MAX_STATUS + 500, null, null, null, null, null, null, null);

            // assert
            assertEquals(MAX_STATUS, mong.getStrength());
        }

        @Test
        @DisplayName("배변 수는 최대 배변 수까지만 올라 간다.")
        void clampsPoopCount() {
            // arrange
            final Mong mong = mong(MongStateCode.NORMAL);

            // act
            mong.adminUpdateStatus(null, null, null, null, null, null, null, Mong.getMaxPoopCount() + 5, null);

            // assert
            assertEquals(Mong.getMaxPoopCount(), mong.getPoopCount());
        }

        @Test
        @DisplayName("경험치가 최대 지수에 닿으면 진화 준비 상태가 된다.")
        void syncsStateCode() {
            // arrange
            final Mong mong = mong(MongStateCode.NORMAL);

            // act
            mong.adminUpdateStatus(null, null, null, null, null, MAX_STATUS, null, null, null);

            // assert
            assertEquals(MongStateCode.EVOLUTION_READY, mong.getStateCode());
        }

        @Test
        @DisplayName("사망 상태에서는 상태 동기화 없이 지수만 바꾼다.")
        void deadMongDoesNotSync() {
            // arrange
            final Mong mong = mong(MongStateCode.DEAD);

            // act
            mong.adminUpdateStatus(null, null, null, null, null, MAX_STATUS, null, null, null);

            // assert
            assertEquals(MAX_STATUS, mong.getExp());
            assertEquals(MongStateCode.DEAD, mong.getStateCode());
        }
    }

    @Nested
    @DisplayName("관리자 상태 코드 변경 단위 테스트")
    class AdminUpdateStateCode {

        @Test
        @DisplayName("사망한 몽을 되살린다.")
        void reviveDeadMong() {
            // arrange
            final Mong mong = mong(MongStateCode.DEAD);

            // act
            mong.adminUpdateStateCode(MongStateCode.NORMAL);

            // assert
            assertEquals(MongStateCode.NORMAL, mong.getStateCode());
            assertTrue(mong.getIsMongStateChange());
        }

        @Test
        @DisplayName("살아 있는 몽을 사망 처리 한다.")
        void killMong() {
            // arrange
            final Mong mong = mong(MongStateCode.NORMAL);

            // act
            mong.adminUpdateStateCode(MongStateCode.DEAD);

            // assert
            assertEquals(MongStateCode.DEAD, mong.getStateCode());
        }

        @Test
        @DisplayName("같은 상태로 바꾸면 변동 플래그가 서지 않는다.")
        void sameStateCode() {
            // arrange
            final Mong mong = mong(MongStateCode.NORMAL);

            // act
            mong.adminUpdateStateCode(MongStateCode.NORMAL);

            // assert
            assertFalse(mong.getIsMongStateChange());
        }
    }
}
