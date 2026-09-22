package com.monglife.mongs.domain.mong.model;

import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import com.monglife.mongs.domain.mong.exception.InvalidEvolutionException;
import com.monglife.mongs.domain.mong.exception.InvalidMongStateException;
import com.monglife.mongs.domain.mong.utils.MongTestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MongTest {

    @Nested
    @DisplayName("몽 사망 단위 테스트")
    class DeadMong {

        @Test
        @DisplayName("몽을 사망 상태로 변경 한다.")
        void deadMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            // act
            mong.dead();

            // assert
            assertEquals(MongStateCode.DEAD, mong.getStateCode());
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void deadMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::dead);
        }

        @Test
        @DisplayName("몽이 진화 준비 상태인 경우 예외가 발생 한다.")
        void deadMongWhenIsGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::dead);
        }
    }

    @Nested
    @DisplayName("몽 쓰다 듬기 단위 테스트")
    class StrokeMong {

        @Test
        @DisplayName("몽을 쓰다 듬고 지수를 증가 시킨다.")
        void strokeMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            // act
            mong.stroke();

            // assert
            assertEquals(1, mong.getStrokeCount());
            assertTrue(mong.getExp() > 0);
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void strokeMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::stroke);
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태인 경우 예외가 발생 한다.")
        void strokeMongWhenIsGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::stroke);
        }

        @Test
        @DisplayName("몽이 알 상태인 경우 예외가 발생 한다.")
        void strokeMongWhenIsEgg() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::stroke);
        }

        @Test
        @DisplayName("몽이 수면 상태인 경우 예외가 발생 한다.")
        void strokeMongWhenIsSleeping() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, true);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::stroke);
        }
    }

    @Nested
    @DisplayName("몽 수면 단위 테스트")
    class SleepMong {

        @Test
        @DisplayName("몽을 수면 상태로 변경 한다.")
        void sleepMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, false);

            // act
            mong.sleep();

            // assert
            assertTrue(mong.getIsSleep());
        }

        @Test
        @DisplayName("몽이 수면 상태인 경우 예외가 발생 한다.")
        void sleepMongWhenIsSleeping() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, true);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::sleep);
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void sleepMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::sleep);
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태인 경우 예외가 발생 한다.")
        void sleepMongWHenIsGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::sleep);
        }

        @Test
        @DisplayName("몽이 알 상태인 경우 예외가 발생 한다.")
        void sleepMongWhenIsEgg() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::sleep);
        }
    }

    @Nested
    @DisplayName("몽 기상 단위 테스트")
    class WakeupMong {

        @Test
        @DisplayName("몽을 기상 상태로 변경 한다.")
        void wakeupMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, true);

            // act
            mong.wakeup();

            // assert
            assertFalse(mong.getIsSleep());
        }

        @Test
        @DisplayName("몽이 기상 상태인 경우 예외가 발생 한다.")
        void wakeupMongWhenIsNotSleeping() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, false);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::wakeup);
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void wakeupMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::wakeup);
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태인 경우 예외가 발생 한다.")
        void wakeupMongWHenIsGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::wakeup);
        }

        @Test
        @DisplayName("몽이 알 상태인 경우 예외가 발생 한다.")
        void wakeupMongWhenIsEgg() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::wakeup);
        }
    }

    @Nested
    @DisplayName("몽 배변 처리 단위 테스트")
    class PoopCleanMong {

        @Test
        @DisplayName("몽 배변을 처리하고 지수를 증가 시킨다.")
        void poopCleanMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final int poopCount = 4;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, poopCount);

            // act
            mong.poopClean();

            // assert
            assertTrue(mong.getExp() > 0);
            assertEquals(0, mong.getPoopCount());
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void poopCleanMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::poopClean);
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태인 경우 예외가 발생 한다.")
        void poopCleanMongWhenIsGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::poopClean);
        }

        @Test
        @DisplayName("몽이 알 상태인 경우 예외가 발생 한다.")
        void poopCleanWhenIsEgg() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::poopClean);
        }

        @Test
        @DisplayName("몽이 수면 상태인 경우 예외가 발생 한다.")
        void poopCleanMongWhenIsSleeping() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, true);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::poopClean);
        }
    }

    @Nested
    @DisplayName("몽 진화 단위 테스트")
    class EvolutionMong {

        @Test
        @DisplayName("몽을 진화 시키고 지수를 재조정 한다.")
        void evolutionMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.EVOLUTION_READY);
            final double nextMaxStatus = 300D;
            final MongType mongType = MongTestUtil.getSecondLevelMongType(nextMaxStatus);

            // act
            mong.evolution(List.of(mongType), Collections.emptyList());

            // assert
            assertNotEquals(MongStateCode.EVOLUTION_READY, mong.getStateCode());
            assertEquals(0D, mong.getEvolutionReward());
            assertEquals(0D, mong.getEvolutionPenalty());
            assertEquals(mongType.getMongCode(), mong.getMongCode());
            assertEquals(mongType.getMongName(), mong.getMongName());
            assertEquals(mongType.getLevel(), mong.getLevel());
            assertEquals(nextMaxStatus, mong.getMaxStatus());
            assertEquals(nextMaxStatus, mong.getStrength());
            assertEquals(nextMaxStatus, mong.getSatiety());
            assertEquals(nextMaxStatus, mong.getHealthy());
            assertEquals(nextMaxStatus, mong.getFatigue());
            assertEquals(0D, mong.getExp());
        }

        @Test
        @DisplayName("몽이 진화 준비 상태가 아닌 경우 예외가 발생 한다.")
        void evolutionMongWhenIsNotEvolutionReady() {
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.NORMAL);
            final double nextMaxStatus = 300D;
            final MongType mongType = MongTestUtil.getSecondLevelMongType(nextMaxStatus);

            // act & assert
            assertThrows(InvalidMongStateException.class, () -> mong.evolution(List.of(mongType), Collections.emptyList()));
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void evolutionMongWhenIsDead() {
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);
            final double nextMaxStatus = 300D;
            final MongType mongType = MongTestUtil.getSecondLevelMongType(nextMaxStatus);

            // act & assert
            assertThrows(InvalidMongStateException.class, () -> mong.evolution(List.of(mongType), Collections.emptyList()));
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태인 경우 예외가 발생 한다.")
        void evolutionMongWHenIsGraduateReady() {
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);
            final double nextMaxStatus = 300D;
            final MongType mongType = MongTestUtil.getSecondLevelMongType(nextMaxStatus);

            // act & assert
            assertThrows(InvalidMongStateException.class, () -> mong.evolution(List.of(mongType), Collections.emptyList()));
        }

        @Test
        @DisplayName("진화 가능한 몽 타입이 없는 경우 예외가 발생 한다.")
        void evolutionMongWHenNotExistsMongTypes() {
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.EVOLUTION_READY);

            // act & assert
            assertThrows(InvalidEvolutionException.class, () -> mong.evolution(Collections.emptyList(), Collections.emptyList()));
        }
    }

    @Nested
    @DisplayName("몽 졸업 단위 테스트")
    class GraduateMong {

        @Test
        @DisplayName("몽을 졸업 상태로 변경 한다.")
        void graduateMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);

            // act
            mong.graduate();

            // assert
            assertEquals(MongStateCode.GRADUATE, mong.getStateCode());
            assertEquals(MongStatusCode.NORMAL, mong.getStatusCode());
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태가 아닌 경우 예외가 발생 한다.")
        void graduateMongWhenIsNotGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.NORMAL);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::graduate);
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void graduateMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::graduate);
        }

        @Test
        @DisplayName("몽이 알 상태인 경우 예외가 발생 한다.")
        void graduateMongWhenIsEgg() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            // act & assert
            assertThrows(InvalidMongStateException.class, mong::graduate);
        }
    }

    @Nested
    @DisplayName("몽 페이 포인트 증가 단위 테스트")
    class IncreaseMongPayPoint {

        @Test
        @DisplayName("몽의 페이 포인트를 증가 시킨다.")
        void increaseMongPayPoint() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final int payPoint = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            // act
            mong.increasePayPoint(payPoint);

            // assert
            assertEquals(payPoint, mong.getPayPoint());
        }
    }

    @Nested
    @DisplayName("몽 지수 증가 단위 테스트")
    class IncreaseMongStatus {

        @Test
        @DisplayName("몽의 지수를 1 cycle 증가 시킨다.")
        void increaseMongStatus() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double status = 0;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, status, maxStatus);

            // act
            mong.cycleIncreaseStatus();

            // assert
            assertTrue(mong.getHealthy() > status);
            assertTrue(mong.getFatigue() > status);
        }
    }

    @Nested
    @DisplayName("몽 지수 감소 단위 테스트")
    class DecreaseMongStatus {

        @Test
        @DisplayName("몽의 지수를 1 cycle 감소 시킨다.")
        void decreaseMongStatus() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            // act
            mong.cycleDecreaseStatus();

            // assert
            assertTrue(mong.getWeight() < maxStatus);
            assertTrue(mong.getStrength() < maxStatus);
            assertTrue(mong.getSatiety() < maxStatus);
            assertTrue(mong.getHealthy() < maxStatus);
            assertTrue(mong.getFatigue() < maxStatus);
        }
    }

    @Nested
    @DisplayName("몽 배변 수 증가 단위 테스트")
    class IncreaseMongPoopCount {

        @Test
        @DisplayName("몽 배변 수를 1 cycle 증가 시킨다.")
        void increaseMongPoopCount() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final int poopCount = 0;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, poopCount);

            // act
            mong.cycleIncreasePoopCount();

            // assert
            assertTrue(mong.getPoopCount() > poopCount);
            assertEquals(0, mong.getEvolutionPenalty());
        }

        @Test
        @DisplayName("이미 최대 배변 수에 도달한 경우 진화 패널티를 1 증가 시킨다.")
        void increaseMongPoopCountWhenAlreadyMaxPoopCount() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final int poopCount = Mong.getMaxPoopCount();
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, poopCount);

            // act
            mong.cycleIncreasePoopCount();

            // assert
            assertEquals(poopCount, mong.getPoopCount());
            assertTrue(mong.getEvolutionPenalty() > 0);
        }
    }

    @Nested
    @DisplayName("훈련 완료 단위 테스트")
    class TrainingEnd {

        @Test
        @DisplayName("몽 훈련 완료 후 지수 감소 및 페이 포인트 보상을 지급 한다.")
        void trainingEnd() {
            // arrange
            final String trainingTypeCode = "TEST-TRAINING-TYPE-CODE";
            final int score = 100;
            final int payPoint = 10;
            final double status = 10D;
            final TrainingType trainingType = new TrainingType(1L, trainingTypeCode, "테스트 훈련 타입", payPoint, score, 60, status, -status, -status, -status, -status);
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            // act
            mong.trainingWithReward(trainingType);

            // assert
            assertEquals(status, mong.getExp());
            assertEquals(maxStatus - status, mong.getStrength());
            assertEquals(maxStatus - status, mong.getSatiety());
            assertEquals(maxStatus - status, mong.getFatigue());
            assertEquals(maxStatus - status, mong.getWeight());
            assertEquals(1, mong.getTrainingCount());
            assertEquals(payPoint, mong.getPayPoint());
        }
    }

    @Nested
    @DisplayName("미션 리워드 경험치 지급 단위 테스트")
    class MissionReward {

        @Test
        @DisplayName("경험치가 지급된다.")
        void missionReward() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);
            final double before = mong.getExp();

            // act
            mong.missionReward(20D);

            // assert
            assertEquals(before + 20D, mong.getExp());
        }

        @Test
        @DisplayName("최대 지수를 넘지 않는다.")
        void missionRewardClampToMaxStatus() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            // act
            mong.missionReward(Double.MAX_VALUE);

            // assert
            assertEquals(maxStatus, mong.getExp());
        }

        @Test
        @DisplayName("경험치가 최대 지수에 도달하면 진화 준비 상태가 된다.")
        void missionRewardSyncStateCode() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            // act
            mong.missionReward(maxStatus);

            // assert - 이 동기화가 빠지면 미션으로 진화 조건을 채워도 진화 버튼이 안 뜬다
            assertEquals(MongStateCode.EVOLUTION_READY, mong.getStateCode());
        }
    }
}
