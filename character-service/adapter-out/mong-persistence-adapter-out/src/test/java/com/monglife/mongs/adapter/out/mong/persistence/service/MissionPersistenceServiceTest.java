package com.monglife.mongs.adapter.out.mong.persistence.service;

import com.monglife.module.common.jpa.config.HibernateAutoConfig;
import com.monglife.module.common.jpa.config.JpaAuditingAutoConfig;
import com.monglife.mongs.adapter.out.mong.persistence.config.AdapterOutMongPersistenceConfig;
import com.monglife.mongs.adapter.out.mong.persistence.config.MongDataSourceConfig;
import com.monglife.mongs.adapter.out.mong.persistence.config.MongRedisConfig;
import com.monglife.mongs.adapter.out.mong.persistence.entity.MissionEntity;
import com.monglife.mongs.adapter.out.mong.persistence.entity.MissionRewardEntity;
import com.monglife.mongs.adapter.out.mong.persistence.repository.MissionRepository;
import com.monglife.mongs.adapter.out.mong.persistence.repository.MissionRewardRepository;
import com.monglife.mongs.adapter.out.mong.persistence.utils.RedisTestContainer;
import com.monglife.mongs.application.mong.port.out.MissionPersistencePort;
import com.monglife.mongs.application.mong.port.out.MissionReadPort;
import com.monglife.mongs.application.mong.port.out.vo.CreateAccountMissionVo;
import com.monglife.mongs.domain.mission.enums.*;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mission.model.Mission;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {
        AdapterOutMongPersistenceConfig.class,
        MongDataSourceConfig.class,
        HibernateAutoConfig.class,
        JpaAuditingAutoConfig.class,
        MongRedisConfig.class
})
class MissionPersistenceServiceTest extends RedisTestContainer {

    private static final long ACCOUNT_ID = 1L;
    private static final String DAILY_KEY = "20260917";

    private final MissionReadPort missionReadPort;
    private final MissionPersistencePort missionPersistencePort;
    private final MissionRepository missionRepository;
    private final MissionRewardRepository missionRewardRepository;

    @Autowired
    public MissionPersistenceServiceTest(MissionReadPort missionReadPort, MissionPersistencePort missionPersistencePort, MissionRepository missionRepository, MissionRewardRepository missionRewardRepository) {
        this.missionReadPort = missionReadPort;
        this.missionPersistencePort = missionPersistencePort;
        this.missionRepository = missionRepository;
        this.missionRewardRepository = missionRewardRepository;
    }

    private MissionEntity givenMission(String missionCode, MissionCycleCode cycleCode, MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, int goalCount) {

        MissionEntity missionEntity = missionRepository.save(MissionEntity.builder()
                .missionCode(missionCode)
                .cycleCode(cycleCode)
                .actionCode(actionCode)
                .goalTypeCode(goalTypeCode)
                .title("TEST-MISSION-TITLE")
                .description("TEST-MISSION-DESCRIPTION")
                .goalCount(goalCount)
                .isActive(true)
                .sortOrder(1)
                .build());

        missionEntity.addReward(missionRewardRepository.save(MissionRewardEntity.builder()
                .mission(missionEntity)
                .rewardTypeCode(MissionRewardTypeCode.INVENTORY)
                .rewardCode("FD000")
                .inventoryTypeCode(InventoryTypeCode.FOOD)
                .amount(2)
                .build()));

        return missionEntity;
    }

    @Nested
    @DisplayName("미션 마스터 조회 단위 테스트")
    class GetActiveMissions {

        @Test
        @DisplayName("활성 미션을 리워드와 함께 조회 한다.")
        void getActiveMissions() {
            // arrange
            givenMission("MS_T_001", MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3);

            // act
            List<Mission> expected = missionReadPort.getActiveMissionsPort(MissionCycleCode.DAILY);

            // assert
            assertEquals(1, expected.size());
            assertEquals(1, expected.get(0).getRewards().size());
            assertEquals("FD000", expected.get(0).getRewards().get(0).getRewardCode());
        }

        @Test
        @DisplayName("다른 주기의 미션은 조회되지 않는다.")
        void getActiveMissionsOfOtherCycle() {
            // arrange
            givenMission("MS_T_002", MissionCycleCode.DAILY, MissionActionCode.STROKE, MissionGoalTypeCode.COUNT, 3);

            // act & assert
            assertTrue(missionReadPort.getActiveMissionsPort(MissionCycleCode.MONTHLY).isEmpty());
        }
    }

    @Nested
    @DisplayName("사용자 미션 적재 단위 테스트")
    class CreateAccountMissions {

        @Test
        @DisplayName("사용자 미션을 적재 한다.")
        void createAccountMissions() {
            // arrange
            MissionEntity mission = givenMission("MS_T_010", MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3);

            // act
            int inserted = missionPersistencePort.createAccountMissionsPort(List.of(vo(mission)));

            // assert
            assertEquals(1, inserted);
            assertEquals(1, missionReadPort.getAccountMissionsPort(ACCOUNT_ID, List.of(DAILY_KEY)).size());
        }

        @Test
        @DisplayName("같은 주기에 같은 미션을 다시 적재해도 예외 없이 건너뛴다.")
        void createAccountMissionsIgnoresDuplicate() {
            // arrange - 앱 기동 때 미션 조회와 플레이 동작이 동시에 들어오는 상황
            MissionEntity mission = givenMission("MS_T_011", MissionCycleCode.DAILY, MissionActionCode.STROKE, MissionGoalTypeCode.COUNT, 3);
            missionPersistencePort.createAccountMissionsPort(List.of(vo(mission)));

            // act
            int inserted = missionPersistencePort.createAccountMissionsPort(List.of(vo(mission)));

            // assert - 예외가 나면 그 트랜잭션이 rollback-only 가 되어 플레이 동작까지 실패한다
            assertEquals(0, inserted);
            assertEquals(1, missionReadPort.getAccountMissionsPort(ACCOUNT_ID, List.of(DAILY_KEY)).size());
        }

        @Test
        @DisplayName("주기 키가 다르면 새 행이 생긴다.")
        void createAccountMissionsForNewCycle() {
            // arrange - 날이 바뀐 상황. 초기화 배치 없이 새 행으로 갈린다
            MissionEntity mission = givenMission("MS_T_012", MissionCycleCode.DAILY, MissionActionCode.WAKEUP, MissionGoalTypeCode.COUNT, 1);
            missionPersistencePort.createAccountMissionsPort(List.of(vo(mission)));

            // act
            int inserted = missionPersistencePort.createAccountMissionsPort(List.of(CreateAccountMissionVo.builder()
                    .accountId(ACCOUNT_ID).missionId(mission.getMissionId())
                    .cycleCode(MissionCycleCode.DAILY).cycleKey("20260918").build()));

            // assert
            assertEquals(1, inserted);
            assertEquals(1, missionReadPort.getAccountMissionsPort(ACCOUNT_ID, List.of(DAILY_KEY)).size());
            assertEquals(1, missionReadPort.getAccountMissionsPort(ACCOUNT_ID, List.of("20260918")).size());
        }

        private CreateAccountMissionVo vo(MissionEntity mission) {
            return CreateAccountMissionVo.builder()
                    .accountId(ACCOUNT_ID)
                    .missionId(mission.getMissionId())
                    .cycleCode(MissionCycleCode.DAILY)
                    .cycleKey(DAILY_KEY)
                    .build();
        }
    }

    @Nested
    @DisplayName("사용자 미션 진행도 저장 단위 테스트")
    class SaveAccountMission {

        @Test
        @DisplayName("COUNT 진행도가 저장 된다.")
        void saveCountProgress() {
            // arrange
            MissionEntity mission = givenMission("MS_T_020", MissionCycleCode.DAILY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3);
            missionPersistencePort.createAccountMissionsPort(List.of(CreateAccountMissionVo.builder()
                    .accountId(ACCOUNT_ID).missionId(mission.getMissionId())
                    .cycleCode(MissionCycleCode.DAILY).cycleKey(DAILY_KEY).build()));

            AccountMission accountMission = missionPersistencePort.getAccountMissionsForUpdatePort(
                    ACCOUNT_ID, List.of(DAILY_KEY), List.of(MissionActionCode.FEED_FOOD)).get(0);

            // act
            accountMission.increaseProgress(null, null);
            missionPersistencePort.saveAccountMissionPort(accountMission);

            // assert
            assertEquals(1, missionReadPort.getAccountMissionsPort(ACCOUNT_ID, List.of(DAILY_KEY)).get(0).getProgressCount());
        }

        @Test
        @DisplayName("DISTINCT 집계 대상이 함께 저장되고, 같은 대상은 다시 세지 않는다.")
        void saveDistinctProgress() {
            // arrange
            MissionEntity mission = givenMission("MS_T_021", MissionCycleCode.WEEKLY, MissionActionCode.FEED_FOOD, MissionGoalTypeCode.DISTINCT, 5);
            missionPersistencePort.createAccountMissionsPort(List.of(CreateAccountMissionVo.builder()
                    .accountId(ACCOUNT_ID).missionId(mission.getMissionId())
                    .cycleCode(MissionCycleCode.WEEKLY).cycleKey("2026-W38").build()));

            AccountMission first = missionPersistencePort.getAccountMissionsForUpdatePort(
                    ACCOUNT_ID, List.of("2026-W38"), List.of(MissionActionCode.FEED_FOOD)).get(0);
            first.increaseProgress(null, "FD000");
            missionPersistencePort.saveAccountMissionPort(first);

            // act - 같은 음식을 다시 먹인다
            AccountMission second = missionPersistencePort.getAccountMissionsForUpdatePort(
                    ACCOUNT_ID, List.of("2026-W38"), List.of(MissionActionCode.FEED_FOOD)).get(0);
            second.increaseProgress(null, "FD000");

            // assert - 이미 집계된 대상이라 오르지 않는다
            assertEquals(1, second.getProgressCount());
            assertFalse(second.getIsProgressChange());
            assertTrue(second.getDetailCodes().contains("FD000"));
        }

        @Test
        @DisplayName("수령 완료 상태와 시각이 저장 된다.")
        void saveClaimed() {
            // arrange
            MissionEntity mission = givenMission("MS_T_022", MissionCycleCode.DAILY, MissionActionCode.SLEEP, MissionGoalTypeCode.COUNT, 1);
            missionPersistencePort.createAccountMissionsPort(List.of(CreateAccountMissionVo.builder()
                    .accountId(ACCOUNT_ID).missionId(mission.getMissionId())
                    .cycleCode(MissionCycleCode.DAILY).cycleKey(DAILY_KEY).build()));

            AccountMission accountMission = missionPersistencePort.getAccountMissionsForUpdatePort(
                    ACCOUNT_ID, List.of(DAILY_KEY), List.of(MissionActionCode.SLEEP)).get(0);
            accountMission.increaseProgress(null, null);
            missionPersistencePort.saveAccountMissionPort(accountMission);

            AccountMission claimable = missionPersistencePort.getAccountMissionForUpdatePort(accountMission.getAccountMissionId()).orElseThrow();

            // act
            claimable.claim(java.time.LocalDateTime.now());
            missionPersistencePort.saveAccountMissionPort(claimable);

            // assert
            AccountMission saved = missionReadPort.getAccountMissionsPort(ACCOUNT_ID, List.of(DAILY_KEY)).get(0);
            assertEquals(MissionStateCode.CLAIMED, saved.getStateCode());
            assertNotNull(saved.getClaimedAt());
        }

        @Test
        @DisplayName("진행 중이 아닌 미션은 갱신 대상에서 빠진다.")
        void excludeNotInProgress() {
            // arrange
            MissionEntity mission = givenMission("MS_T_023", MissionCycleCode.DAILY, MissionActionCode.POOP_CLEAN, MissionGoalTypeCode.COUNT, 1);
            missionPersistencePort.createAccountMissionsPort(List.of(CreateAccountMissionVo.builder()
                    .accountId(ACCOUNT_ID).missionId(mission.getMissionId())
                    .cycleCode(MissionCycleCode.DAILY).cycleKey(DAILY_KEY).build()));

            AccountMission accountMission = missionPersistencePort.getAccountMissionsForUpdatePort(
                    ACCOUNT_ID, List.of(DAILY_KEY), List.of(MissionActionCode.POOP_CLEAN)).get(0);
            accountMission.increaseProgress(null, null);
            missionPersistencePort.saveAccountMissionPort(accountMission);

            // act
            List<AccountMission> expected = missionPersistencePort.getAccountMissionsForUpdatePort(
                    ACCOUNT_ID, List.of(DAILY_KEY), List.of(MissionActionCode.POOP_CLEAN));

            // assert - 이미 수령 가능 상태라 더 올릴 것이 없다
            assertTrue(expected.isEmpty());
        }
    }
}
