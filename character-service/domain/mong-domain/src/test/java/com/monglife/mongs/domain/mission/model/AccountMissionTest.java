package com.monglife.mongs.domain.mission.model;

import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.enums.MissionStateCode;
import com.monglife.mongs.domain.mission.exception.AlreadyClaimedMissionException;
import com.monglife.mongs.domain.mission.exception.ForbiddenMissionException;
import com.monglife.mongs.domain.mission.exception.NotClaimableMissionException;
import com.monglife.mongs.domain.mission.utils.MissionTestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AccountMissionTest {

    private static final long ACCOUNT_ID = 1L;

    @Nested
    @DisplayName("미션 권한 확인 단위 테스트")
    class Verify {

        @Test
        @DisplayName("다른 계정의 미션이면 예외가 발생 한다.")
        void verifyWhenOtherAccount() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(ACCOUNT_ID, mission);

            // act & assert
            assertThrows(ForbiddenMissionException.class, () -> accountMission.verify(999L));
        }
    }

    @Nested
    @DisplayName("COUNT 미션 진행도 단위 테스트")
    class CountProgress {

        @Test
        @DisplayName("액션 한 번에 1씩 오른다.")
        void increaseByOne() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(ACCOUNT_ID, mission);

            // act
            accountMission.increaseProgress(null, null);

            // assert
            assertEquals(1, accountMission.getProgressCount());
            assertTrue(accountMission.getIsProgressChange());
            assertEquals(MissionStateCode.IN_PROGRESS, accountMission.getStateCode());
        }

        @Test
        @DisplayName("같은 대상을 반복해도 계속 오른다.")
        void increaseWithSameDetail() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(ACCOUNT_ID, mission);

            // act
            accountMission.increaseProgress(null, "FD000");
            accountMission.increaseProgress(null, "FD000");

            // assert
            assertEquals(2, accountMission.getProgressCount());
        }

        @Test
        @DisplayName("목표에 도달하면 수령 가능 상태가 된다.")
        void becomeClaimable() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 2);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(ACCOUNT_ID, mission);

            // act
            accountMission.increaseProgress(null, null);
            accountMission.increaseProgress(null, null);

            // assert
            assertEquals(MissionStateCode.CLAIMABLE, accountMission.getStateCode());
            assertTrue(accountMission.getIsClaimableChange());
        }

        @Test
        @DisplayName("수령 가능 상태가 된 뒤에는 더 오르지 않는다.")
        void notIncreaseAfterClaimable() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 1);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(ACCOUNT_ID, mission);
            accountMission.increaseProgress(null, null);

            // act
            accountMission.increaseProgress(null, null);

            // assert - 주기마다 새 행이라 초과분이 이월될 곳이 없다
            assertEquals(1, accountMission.getProgressCount());
            assertFalse(accountMission.getIsProgressChange());
        }
    }

    @Nested
    @DisplayName("DISTINCT 미션 진행도 단위 테스트")
    class DistinctProgress {

        @Test
        @DisplayName("처음 보는 대상이면 오르고 집계 대상으로 기록된다.")
        void increaseWithNewDetail() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.WEEKLY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.DISTINCT, 5);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(ACCOUNT_ID, mission);

            // act
            accountMission.increaseProgress(null, "FD000");

            // assert
            assertEquals(1, accountMission.getProgressCount());
            assertEquals("FD000", accountMission.getAddedDetailCode());
            assertTrue(accountMission.getDetailCodes().contains("FD000"));
        }

        @Test
        @DisplayName("이미 집계된 대상이면 오르지 않는다.")
        void notIncreaseWithDuplicatedDetail() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.WEEKLY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.DISTINCT, 5);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(
                    ACCOUNT_ID, mission, 1, MissionStateCode.IN_PROGRESS, Set.of("FD000"));

            // act
            accountMission.increaseProgress(null, "FD000");

            // assert
            assertEquals(1, accountMission.getProgressCount());
            assertFalse(accountMission.getIsProgressChange());
            assertNull(accountMission.getAddedDetailCode());
        }

        @Test
        @DisplayName("대상 코드가 없으면 오르지 않는다.")
        void notIncreaseWithoutDetail() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.WEEKLY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.DISTINCT, 5);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(ACCOUNT_ID, mission);

            // act
            accountMission.increaseProgress(null, null);

            // assert
            assertEquals(0, accountMission.getProgressCount());
            assertFalse(accountMission.getIsProgressChange());
        }
    }

    @Nested
    @DisplayName("ACCUMULATE 미션 진행도 단위 테스트")
    class AccumulateProgress {

        @Test
        @DisplayName("넘긴 값만큼 오른다.")
        void increaseByAmount() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.WEEKLY, MissionActionCode.PAY_POINT_SPEND, MissionGoalTypeCode.ACCUMULATE, 500);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(ACCOUNT_ID, mission);

            // act
            accountMission.increaseProgress(120, null);
            accountMission.increaseProgress(80, null);

            // assert
            assertEquals(200, accountMission.getProgressCount());
        }

        @Test
        @DisplayName("목표를 넘겨도 목표치까지만 기록된다.")
        void clampToGoal() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.WEEKLY, MissionActionCode.PAY_POINT_SPEND, MissionGoalTypeCode.ACCUMULATE, 500);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(ACCOUNT_ID, mission);

            // act
            accountMission.increaseProgress(9999, null);

            // assert
            assertEquals(500, accountMission.getProgressCount());
            assertEquals(MissionStateCode.CLAIMABLE, accountMission.getStateCode());
        }

        @Test
        @DisplayName("값이 없으면 오르지 않는다.")
        void notIncreaseWithoutAmount() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.WEEKLY, MissionActionCode.PAY_POINT_SPEND, MissionGoalTypeCode.ACCUMULATE, 500);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(ACCOUNT_ID, mission);

            // act
            accountMission.increaseProgress(null, null);

            // assert
            assertEquals(0, accountMission.getProgressCount());
        }
    }

    @Nested
    @DisplayName("미션 리워드 수령 단위 테스트")
    class Claim {

        @Test
        @DisplayName("수령 가능 상태면 수령 완료로 바뀐다.")
        void claim() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 1);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(
                    ACCOUNT_ID, mission, 1, MissionStateCode.CLAIMABLE, Set.of());
            final LocalDateTime claimedAt = LocalDateTime.now();

            // act
            accountMission.claim(claimedAt);

            // assert
            assertEquals(MissionStateCode.CLAIMED, accountMission.getStateCode());
            assertEquals(claimedAt, accountMission.getClaimedAt());
        }

        @Test
        @DisplayName("아직 달성하지 못했으면 예외가 발생 한다.")
        void claimWhenInProgress() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(ACCOUNT_ID, mission);

            // act & assert
            assertThrows(NotClaimableMissionException.class, () -> accountMission.claim(LocalDateTime.now()));
        }

        @Test
        @DisplayName("이미 수령했으면 예외가 발생 한다.")
        void claimWhenAlreadyClaimed() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 1);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(
                    ACCOUNT_ID, mission, 1, MissionStateCode.CLAIMED, Set.of());

            // act & assert
            assertThrows(AlreadyClaimedMissionException.class, () -> accountMission.claim(LocalDateTime.now()));
        }

        @Test
        @DisplayName("수령 완료된 미션은 진행도가 더 오르지 않는다.")
        void notIncreaseAfterClaimed() {
            // arrange
            final Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 5);
            final AccountMission accountMission = MissionTestUtil.getAccountMission(
                    ACCOUNT_ID, mission, 5, MissionStateCode.CLAIMED, Set.of());

            // act
            accountMission.increaseProgress(null, null);

            // assert
            assertEquals(5, accountMission.getProgressCount());
            assertFalse(accountMission.getIsProgressChange());
        }
    }
}
