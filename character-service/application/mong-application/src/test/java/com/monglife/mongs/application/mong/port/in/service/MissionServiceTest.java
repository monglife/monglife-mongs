package com.monglife.mongs.application.mong.port.in.service;

import com.monglife.mongs.application.mong.port.exception.NotExistsMissionException;
import com.monglife.mongs.application.mong.port.in.MissionUseCase;
import com.monglife.mongs.application.mong.port.in.command.ClaimMissionRewardCommand;
import com.monglife.mongs.application.mong.port.in.command.GetMissionsCommand;
import com.monglife.mongs.application.mong.port.in.command.IncreaseMissionProgressCommand;
import com.monglife.mongs.application.mong.port.in.utils.MissionTestUtil;
import com.monglife.mongs.application.mong.port.in.utils.MongTestUtil;
import com.monglife.mongs.application.mong.port.out.MissionPersistencePort;
import com.monglife.mongs.application.mong.port.out.MissionReadPort;
import com.monglife.mongs.application.mong.port.out.MongEventPort;
import com.monglife.mongs.application.mong.port.out.MongPersistencePort;
import com.monglife.mongs.application.mong.port.out.vo.CreateAccountMissionVo;
import com.monglife.mongs.application.mong.port.out.vo.CreateInventoryVo;
import com.monglife.mongs.domain.mission.enums.*;
import com.monglife.mongs.domain.mission.exception.AlreadyClaimedMissionException;
import com.monglife.mongs.domain.mission.exception.ForbiddenMissionException;
import com.monglife.mongs.domain.mission.exception.NotClaimableMissionException;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mission.model.Mission;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import com.monglife.mongs.domain.mong.model.Inventory;
import com.monglife.mongs.domain.mong.model.Mong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MissionServiceTest {

    private static final long ACCOUNT_ID = 1L;
    private static final long MONG_ID = 1L;
    private static final double MAX_STATUS = 100D;
    private static final int DAILY_PICK_COUNT = 5;

    private static final String DAILY_KEY = MissionCycleCode.DAILY.cycleKey(LocalDate.now(ZoneId.of("Asia/Seoul")));
    private static final String WEEKLY_KEY = MissionCycleCode.WEEKLY.cycleKey(LocalDate.now(ZoneId.of("Asia/Seoul")));
    private static final String MONTHLY_KEY = MissionCycleCode.MONTHLY.cycleKey(LocalDate.now(ZoneId.of("Asia/Seoul")));

    private final MissionReadPort missionReadPort = Mockito.mock(MissionReadPort.class);
    private final MissionPersistencePort missionPersistencePort = Mockito.mock(MissionPersistencePort.class);
    private final MongPersistencePort mongPersistencePort = Mockito.mock(MongPersistencePort.class);
    private final MongEventPort mongEventPort = Mockito.mock(MongEventPort.class);

    private final MissionUseCase missionUseCase =
            new MissionService(missionReadPort, missionPersistencePort, mongPersistencePort, mongEventPort, DAILY_PICK_COUNT);

    /** 액션 10종 x 난이도 2단계의 일간 마스터. 실제 시드와 같은 모양이다 */
    private static List<Mission> dailyMasters() {

        MissionActionCode[] actionCodes = {
                MissionActionCode.FEED_FOOD, MissionActionCode.FEED_SNACK, MissionActionCode.STROKE,
                MissionActionCode.POOP_CLEAN, MissionActionCode.TRAINING_END, MissionActionCode.RANDOM_DRAW,
                MissionActionCode.BUY_RANDOM_DRAW_TICKET, MissionActionCode.USE_INVENTORY,
                MissionActionCode.SLEEP, MissionActionCode.WAKEUP
        };

        List<Mission> missions = new ArrayList<>();
        long missionId = 1L;

        for (MissionActionCode actionCode : actionCodes) {
            for (int goalCount : new int[] { 3, 6 }) {
                missions.add(MissionTestUtil.getMission(missionId++, MissionCycleCode.DAILY, actionCode, MissionGoalTypeCode.COUNT, goalCount));
            }
        }

        return missions;
    }

    @Nested
    @DisplayName("미션 목록 조회 단위 테스트")
    class GetMissionsUseCase {

        @Test
        @DisplayName("이번 주기 미션이 없으면 적재한다.")
        void getMissionsWhenEmpty() {
            // arrange
            Mockito.when(missionReadPort.getAccountMissionsPort(Mockito.eq(ACCOUNT_ID), Mockito.any()))
                    .thenReturn(List.of());
            Mockito.when(missionReadPort.getActiveMissionsPort(MissionCycleCode.DAILY)).thenReturn(dailyMasters());
            Mockito.when(missionReadPort.getActiveMissionsPort(MissionCycleCode.WEEKLY)).thenReturn(List.of(
                    MissionTestUtil.getMission(100L, MissionCycleCode.WEEKLY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.DISTINCT, 5)));
            Mockito.when(missionReadPort.getActiveMissionsPort(MissionCycleCode.MONTHLY)).thenReturn(List.of(
                    MissionTestUtil.getMission(200L, MissionCycleCode.MONTHLY, MissionActionCode.EVOLUTION, MissionGoalTypeCode.COUNT, 1)));

            // act
            missionUseCase.getMissionsUseCase(GetMissionsCommand.builder().accountId(ACCOUNT_ID).build());

            // assert
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<CreateAccountMissionVo>> captor = ArgumentCaptor.forClass(List.class);
            Mockito.verify(missionPersistencePort).createAccountMissionsPort(captor.capture());

            List<CreateAccountMissionVo> created = captor.getValue();
            assertEquals(DAILY_PICK_COUNT + 2, created.size());

            assertEquals(DAILY_PICK_COUNT, created.stream().filter(vo -> vo.getCycleCode() == MissionCycleCode.DAILY).count());
            assertEquals(1, created.stream().filter(vo -> vo.getCycleCode() == MissionCycleCode.WEEKLY).count());
            assertEquals(1, created.stream().filter(vo -> vo.getCycleCode() == MissionCycleCode.MONTHLY).count());
        }

        @Test
        @DisplayName("일간 미션은 액션이 겹치지 않게 뽑는다.")
        void getMissionsPicksDistinctActions() {
            // arrange
            List<Mission> masters = dailyMasters();
            Mockito.when(missionReadPort.getAccountMissionsPort(Mockito.eq(ACCOUNT_ID), Mockito.any())).thenReturn(List.of());
            Mockito.when(missionReadPort.getActiveMissionsPort(MissionCycleCode.DAILY)).thenReturn(masters);
            Mockito.when(missionReadPort.getActiveMissionsPort(MissionCycleCode.WEEKLY)).thenReturn(List.of());
            Mockito.when(missionReadPort.getActiveMissionsPort(MissionCycleCode.MONTHLY)).thenReturn(List.of());

            // act
            missionUseCase.getMissionsUseCase(GetMissionsCommand.builder().accountId(ACCOUNT_ID).build());

            // assert - "밥 3번"과 "밥 6번"이 같은 날 함께 나오면 다섯 칸 중 둘이 같은 미션이 된다
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<CreateAccountMissionVo>> captor = ArgumentCaptor.forClass(List.class);
            Mockito.verify(missionPersistencePort).createAccountMissionsPort(captor.capture());

            List<MissionActionCode> pickedActions = captor.getValue().stream()
                    .map(vo -> masters.stream()
                            .filter(mission -> mission.getMissionId().equals(vo.getMissionId()))
                            .findFirst()
                            .orElseThrow()
                            .getActionCode())
                    .toList();

            assertEquals(DAILY_PICK_COUNT, pickedActions.size());
            assertEquals(DAILY_PICK_COUNT, pickedActions.stream().distinct().count());
        }

        @Test
        @DisplayName("이미 적재된 주기는 다시 적재하지 않는다.")
        void getMissionsWhenAlreadyFilled() {
            // arrange
            Mission daily = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3);
            Mission weekly = MissionTestUtil.getMission(2L, MissionCycleCode.WEEKLY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.DISTINCT, 5);
            Mission monthly = MissionTestUtil.getMission(3L, MissionCycleCode.MONTHLY, MissionActionCode.EVOLUTION, MissionGoalTypeCode.COUNT, 1);

            Mockito.when(missionReadPort.getAccountMissionsPort(Mockito.eq(ACCOUNT_ID), Mockito.any())).thenReturn(List.of(
                    MissionTestUtil.getAccountMission(1L, ACCOUNT_ID, daily, DAILY_KEY),
                    MissionTestUtil.getAccountMission(2L, ACCOUNT_ID, weekly, WEEKLY_KEY),
                    MissionTestUtil.getAccountMission(3L, ACCOUNT_ID, monthly, MONTHLY_KEY)));

            // act
            List<AccountMission> expected = missionUseCase.getMissionsUseCase(GetMissionsCommand.builder().accountId(ACCOUNT_ID).build());

            // assert - 재조회해도 그날 뽑힌 일간 미션이 바뀌지 않아야 한다
            assertEquals(3, expected.size());
            Mockito.verify(missionPersistencePort, Mockito.never()).createAccountMissionsPort(Mockito.any());
        }
    }

    @Nested
    @DisplayName("미션 진행도 반영 단위 테스트")
    class IncreaseMissionProgressUseCase {

        @Test
        @DisplayName("한 번의 액션이 일간 COUNT 와 주간 DISTINCT 를 함께 올린다.")
        void increaseProgressAcrossCycles() {
            // arrange
            Mission daily = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3);
            Mission weekly = MissionTestUtil.getMission(2L, MissionCycleCode.WEEKLY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.DISTINCT, 5);

            AccountMission dailyAccountMission = MissionTestUtil.getAccountMission(1L, ACCOUNT_ID, daily, DAILY_KEY);
            AccountMission weeklyAccountMission = MissionTestUtil.getAccountMission(2L, ACCOUNT_ID, weekly, WEEKLY_KEY);

            givenFilledCycles(dailyAccountMission, weeklyAccountMission);
            Mockito.when(missionPersistencePort.getAccountMissionsForUpdatePort(Mockito.eq(ACCOUNT_ID), Mockito.any(), Mockito.any()))
                    .thenReturn(List.of(dailyAccountMission, weeklyAccountMission));

            // act
            missionUseCase.increaseMissionProgressUseCase(IncreaseMissionProgressCommand.builder()
                    .accountId(ACCOUNT_ID)
                    .actionCode(MissionActionCode.FEED_FOOD)
                    .detailCode("FD000")
                    .build());

            // assert
            assertEquals(1, dailyAccountMission.getProgressCount());
            assertEquals(1, weeklyAccountMission.getProgressCount());
            assertEquals("FD000", weeklyAccountMission.getAddedDetailCode());
            Mockito.verify(missionPersistencePort, Mockito.times(2)).saveAccountMissionPort(Mockito.any());
        }

        @Test
        @DisplayName("같은 대상을 다시 쓰면 DISTINCT 는 오르지 않고 COUNT 만 오른다.")
        void increaseProgressWithDuplicatedDetail() {
            // arrange
            Mission daily = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3);
            Mission weekly = MissionTestUtil.getMission(2L, MissionCycleCode.WEEKLY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.DISTINCT, 5);

            AccountMission dailyAccountMission = MissionTestUtil.getAccountMission(1L, ACCOUNT_ID, daily, DAILY_KEY);
            AccountMission weeklyAccountMission = AccountMission.builder()
                    .accountMissionId(2L)
                    .accountId(ACCOUNT_ID)
                    .mission(weekly)
                    .cycleKey(WEEKLY_KEY)
                    .progressCount(1)
                    .stateCode(MissionStateCode.IN_PROGRESS)
                    .detailCodes(java.util.Set.of("FD000"))
                    .build();

            givenFilledCycles(dailyAccountMission, weeklyAccountMission);
            Mockito.when(missionPersistencePort.getAccountMissionsForUpdatePort(Mockito.eq(ACCOUNT_ID), Mockito.any(), Mockito.any()))
                    .thenReturn(List.of(dailyAccountMission, weeklyAccountMission));

            // act
            missionUseCase.increaseMissionProgressUseCase(IncreaseMissionProgressCommand.builder()
                    .accountId(ACCOUNT_ID)
                    .actionCode(MissionActionCode.FEED_FOOD)
                    .detailCode("FD000")
                    .build());

            // assert
            assertEquals(1, dailyAccountMission.getProgressCount());
            assertEquals(1, weeklyAccountMission.getProgressCount());
            Mockito.verify(missionPersistencePort, Mockito.times(1)).saveAccountMissionPort(Mockito.any());
        }

        @Test
        @DisplayName("몽 돌본 날 미션은 어떤 액션에서든 오늘 날짜로 오른다.")
        void increaseCareDayProgress() {
            // arrange
            Mission careDay = MissionTestUtil.getMission(3L, MissionCycleCode.WEEKLY, MissionActionCode.CARE_DAY, MissionGoalTypeCode.DISTINCT, 5);
            AccountMission careDayAccountMission = MissionTestUtil.getAccountMission(3L, ACCOUNT_ID, careDay, WEEKLY_KEY);

            givenFilledCycles(careDayAccountMission);
            Mockito.when(missionPersistencePort.getAccountMissionsForUpdatePort(Mockito.eq(ACCOUNT_ID), Mockito.any(), Mockito.any()))
                    .thenReturn(List.of(careDayAccountMission));

            // act - 액션은 밥 주기인데 CARE_DAY 는 음식 코드가 아니라 날짜로 집계돼야 한다
            missionUseCase.increaseMissionProgressUseCase(IncreaseMissionProgressCommand.builder()
                    .accountId(ACCOUNT_ID)
                    .actionCode(MissionActionCode.FEED_FOOD)
                    .detailCode("FD000")
                    .build());

            // assert
            assertEquals(1, careDayAccountMission.getProgressCount());
            assertEquals(DAILY_KEY, careDayAccountMission.getAddedDetailCode());
        }

        @Test
        @DisplayName("대상 코드가 없는 액션의 DISTINCT 미션은 날짜로 집계된다.")
        void increaseDistinctWithoutDetailFallsBackToDate() {
            // arrange - "이번 달 20일 재워주기"
            Mission monthlySleep = MissionTestUtil.getMission(4L, MissionCycleCode.MONTHLY, MissionActionCode.SLEEP, MissionGoalTypeCode.DISTINCT, 20);
            AccountMission accountMission = MissionTestUtil.getAccountMission(4L, ACCOUNT_ID, monthlySleep, MONTHLY_KEY);

            givenFilledCycles(accountMission);
            Mockito.when(missionPersistencePort.getAccountMissionsForUpdatePort(Mockito.eq(ACCOUNT_ID), Mockito.any(), Mockito.any()))
                    .thenReturn(List.of(accountMission));

            // act - 하루에 두 번 재워도 하루로 센다
            missionUseCase.increaseMissionProgressUseCase(IncreaseMissionProgressCommand.builder()
                    .accountId(ACCOUNT_ID).actionCode(MissionActionCode.SLEEP).build());
            missionUseCase.increaseMissionProgressUseCase(IncreaseMissionProgressCommand.builder()
                    .accountId(ACCOUNT_ID).actionCode(MissionActionCode.SLEEP).build());

            // assert
            assertEquals(1, accountMission.getProgressCount());
            assertEquals(DAILY_KEY, accountMission.getDetailCodes().iterator().next());
        }

        @Test
        @DisplayName("미션 목록을 한 번도 열지 않았어도 진행도가 쌓인다.")
        void increaseProgressMaterializesMissions() {
            // arrange - 적재된 것이 없는 상태
            Mockito.when(missionReadPort.getAccountMissionsPort(Mockito.eq(ACCOUNT_ID), Mockito.any())).thenReturn(List.of());
            Mockito.when(missionReadPort.getActiveMissionsPort(Mockito.any())).thenReturn(List.of(
                    MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3)));
            Mockito.when(missionPersistencePort.getAccountMissionsForUpdatePort(Mockito.eq(ACCOUNT_ID), Mockito.any(), Mockito.any()))
                    .thenReturn(List.of());

            // act
            missionUseCase.increaseMissionProgressUseCase(IncreaseMissionProgressCommand.builder()
                    .accountId(ACCOUNT_ID)
                    .actionCode(MissionActionCode.FEED_FOOD)
                    .build());

            // assert
            Mockito.verify(missionPersistencePort).createAccountMissionsPort(Mockito.any());
        }

        private void givenFilledCycles(AccountMission... accountMissions) {
            // 세 주기가 모두 적재된 것으로 보이게 한다 - 적재 로직이 끼어들지 않게
            List<AccountMission> filled = new ArrayList<>(List.of(accountMissions));
            for (MissionCycleCode cycleCode : MissionCycleCode.values()) {
                boolean exists = filled.stream().anyMatch(am -> am.getMission().getCycleCode() == cycleCode);
                if (!exists) {
                    filled.add(MissionTestUtil.getAccountMission(900L + cycleCode.ordinal(), ACCOUNT_ID,
                            MissionTestUtil.getMission(900L + cycleCode.ordinal(), cycleCode, MissionActionCode.GRADUATE, MissionGoalTypeCode.COUNT, 1),
                            cycleCode.cycleKey(LocalDate.now(ZoneId.of("Asia/Seoul")))));
                }
            }
            Mockito.when(missionReadPort.getAccountMissionsPort(Mockito.eq(ACCOUNT_ID), (Collection<String>) Mockito.any()))
                    .thenReturn(filled);
        }
    }

    @Nested
    @DisplayName("미션 리워드 수령 단위 테스트")
    class ClaimMissionRewardUseCase {

        @Test
        @DisplayName("리워드 4종이 각자의 경로로 지급된다.")
        void claimAllRewardTypes() {
            // arrange
            Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.MONTHLY, MissionActionCode.GRADUATE, MissionGoalTypeCode.COUNT, 1, List.of(
                    MissionTestUtil.reward(MissionRewardTypeCode.EXP, 10),
                    MissionTestUtil.reward(MissionRewardTypeCode.PAY_POINT, 500),
                    MissionTestUtil.reward(MissionRewardTypeCode.STAR_POINT, 3),
                    MissionTestUtil.inventoryReward("FD000", InventoryTypeCode.FOOD, 2)));

            AccountMission accountMission = MissionTestUtil.getAccountMission(1L, ACCOUNT_ID, mission, MONTHLY_KEY, 1, MissionStateCode.CLAIMABLE);
            Mong mong = MongTestUtil.getFirstLevelMong(MONG_ID, ACCOUNT_ID, MAX_STATUS);

            Mockito.when(missionPersistencePort.getAccountMissionForUpdatePort(1L)).thenReturn(Optional.of(accountMission));
            Mockito.when(mongPersistencePort.getMongPort(MONG_ID)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(Mockito.any())).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.createInventoryPort(Mockito.any()))
                    .thenReturn(Optional.of(Inventory.builder().inventoryId(1L).mongId(MONG_ID).build()));

            final double expBefore = mong.getExp();
            final int payPointBefore = mong.getPayPoint();

            // act
            Mong expected = missionUseCase.claimMissionRewardUseCase(ClaimMissionRewardCommand.builder()
                    .accountId(ACCOUNT_ID).accountMissionId(1L).mongId(MONG_ID).build());

            // assert
            assertEquals(expBefore + 10D, expected.getExp());
            assertEquals(payPointBefore + 500, expected.getPayPoint());
            assertEquals(MissionStateCode.CLAIMED, accountMission.getStateCode());

            // 인벤 아이템은 수량만큼 따로 들어간다
            Mockito.verify(mongPersistencePort, Mockito.times(2)).createInventoryPort(Mockito.any(CreateInventoryVo.class));
            // 스타 포인트는 Player 소유라 이벤트로 넘긴다
            Mockito.verify(mongEventPort).missionRewardStarPointEventPort(ACCOUNT_ID, 3, mission.getMissionCode());
        }

        @Test
        @DisplayName("지급보다 먼저 수령 완료로 바꾼다.")
        void claimMarksBeforeGranting() {
            // arrange
            Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 1, List.of(
                    MissionTestUtil.reward(MissionRewardTypeCode.PAY_POINT, 30)));
            AccountMission accountMission = MissionTestUtil.getAccountMission(1L, ACCOUNT_ID, mission, DAILY_KEY, 1, MissionStateCode.CLAIMABLE);
            Mong mong = MongTestUtil.getFirstLevelMong(MONG_ID, ACCOUNT_ID, MAX_STATUS);

            Mockito.when(missionPersistencePort.getAccountMissionForUpdatePort(1L)).thenReturn(Optional.of(accountMission));
            Mockito.when(mongPersistencePort.getMongPort(MONG_ID)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(Mockito.any())).thenReturn(Optional.of(mong));

            // act
            missionUseCase.claimMissionRewardUseCase(ClaimMissionRewardCommand.builder()
                    .accountId(ACCOUNT_ID).accountMissionId(1L).mongId(MONG_ID).build());

            // assert - 수령 기록이 먼저 저장돼야 같은 행이 두 번 지급되는 경로가 없다
            Mockito.verify(missionPersistencePort).saveAccountMissionPort(accountMission);
        }

        @Test
        @DisplayName("존재하지 않는 미션이면 예외가 발생 한다.")
        void claimWhenNotExists() {
            // arrange
            Mockito.when(missionPersistencePort.getAccountMissionForUpdatePort(1L)).thenReturn(Optional.empty());

            // act & assert
            assertThrows(NotExistsMissionException.class, () -> missionUseCase.claimMissionRewardUseCase(
                    ClaimMissionRewardCommand.builder().accountId(ACCOUNT_ID).accountMissionId(1L).mongId(MONG_ID).build()));
        }

        @Test
        @DisplayName("다른 계정의 미션이면 예외가 발생 한다.")
        void claimWhenOtherAccount() {
            // arrange
            Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 1);
            AccountMission accountMission = MissionTestUtil.getAccountMission(1L, ACCOUNT_ID, mission, DAILY_KEY, 1, MissionStateCode.CLAIMABLE);
            Mockito.when(missionPersistencePort.getAccountMissionForUpdatePort(1L)).thenReturn(Optional.of(accountMission));

            // act & assert
            assertThrows(ForbiddenMissionException.class, () -> missionUseCase.claimMissionRewardUseCase(
                    ClaimMissionRewardCommand.builder().accountId(999L).accountMissionId(1L).mongId(MONG_ID).build()));
        }

        @Test
        @DisplayName("아직 달성하지 못했으면 예외가 발생 한다.")
        void claimWhenInProgress() {
            // arrange
            Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3);
            AccountMission accountMission = MissionTestUtil.getAccountMission(1L, ACCOUNT_ID, mission, DAILY_KEY);
            Mockito.when(missionPersistencePort.getAccountMissionForUpdatePort(1L)).thenReturn(Optional.of(accountMission));

            // act & assert
            assertThrows(NotClaimableMissionException.class, () -> missionUseCase.claimMissionRewardUseCase(
                    ClaimMissionRewardCommand.builder().accountId(ACCOUNT_ID).accountMissionId(1L).mongId(MONG_ID).build()));
        }

        @Test
        @DisplayName("이미 수령했으면 예외가 발생 한다.")
        void claimWhenAlreadyClaimed() {
            // arrange
            Mission mission = MissionTestUtil.getMission(1L, MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 1);
            AccountMission accountMission = MissionTestUtil.getAccountMission(1L, ACCOUNT_ID, mission, DAILY_KEY, 1, MissionStateCode.CLAIMED);
            Mockito.when(missionPersistencePort.getAccountMissionForUpdatePort(1L)).thenReturn(Optional.of(accountMission));

            // act & assert
            assertThrows(AlreadyClaimedMissionException.class, () -> missionUseCase.claimMissionRewardUseCase(
                    ClaimMissionRewardCommand.builder().accountId(ACCOUNT_ID).accountMissionId(1L).mongId(MONG_ID).build()));
        }
    }
}
