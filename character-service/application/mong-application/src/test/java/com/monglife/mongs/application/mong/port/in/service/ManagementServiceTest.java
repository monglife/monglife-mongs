package com.monglife.mongs.application.mong.port.in.service;

import com.monglife.mongs.application.mong.port.exception.InvalidStrokeMongException;
import com.monglife.mongs.application.mong.port.exception.NotExistsMongException;
import com.monglife.mongs.application.mong.port.in.ManagementUseCase;
import com.monglife.mongs.application.mong.port.in.MissionUseCase;
import com.monglife.mongs.application.mong.port.in.command.*;
import com.monglife.mongs.application.mong.port.in.utils.MongTestUtil;
import com.monglife.mongs.application.mong.port.out.MongEventPort;
import com.monglife.mongs.application.mong.port.out.MongPersistencePort;
import com.monglife.mongs.application.mong.port.out.MongReadPort;
import com.monglife.mongs.application.mong.port.out.MongSchedulerPort;
import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import com.monglife.mongs.domain.mong.exception.ForbiddenMongException;
import com.monglife.mongs.domain.mong.exception.InvalidEvolutionException;
import com.monglife.mongs.domain.mong.exception.InvalidMongStateException;
import com.monglife.mongs.domain.mong.model.Mong;
import com.monglife.mongs.domain.mong.model.MongType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ManagementServiceTest {

    private final MongSchedulerPort mongSchedulerPort = Mockito.mock(MongSchedulerPort.class);
    private final MongPersistencePort mongPersistencePort = Mockito.mock(MongPersistencePort.class);
    private final MongReadPort mongReadPort = Mockito.mock(MongReadPort.class);
    private final MongEventPort mongEventPort = Mockito.mock(MongEventPort.class);
    private final MissionUseCase missionUseCase = Mockito.mock(MissionUseCase.class);
    private final ManagementUseCase managementUseCase = new ManagementService(mongSchedulerPort, mongPersistencePort, mongReadPort, mongEventPort, missionUseCase);

    @Nested
    @DisplayName("몽 생성 단위 테스트")
    class CreateMongUseCase {

        @Test
        @DisplayName("새로운 알 상태의 몽을 생성 한다.")
        void createMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);
            final MongType mongType =  MongTestUtil.getEggMongType(maxStatus);

            Mockito.when(mongReadPort.getMongTypesPort(Mockito.any())).thenReturn(List.of(mongType));
            Mockito.when(mongPersistencePort.createMongPort(Mockito.any())).thenReturn(Optional.of(mong));
            Mockito.when(mongSchedulerPort.createTaskPort(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Optional.of(Mockito.anyLong()));

            // act
            CreateMongCommand command = CreateMongCommand.builder()
                    .accountId(accountId)
                    .name(mong.getName())
                    .sleepAt(mong.getSleepAt())
                    .wakeupAt(mong.getWakeupAt())
                    .build();

            var expected = managementUseCase.createMongUseCase(command);

            // assert
            Mockito.verify(mongEventPort).createMongEventPort(accountId, mongType.getMongCode());
            assertEquals(mongId, expected.getMongId());
            assertEquals(accountId, expected.getAccountId());
            assertEquals(maxStatus, expected.getMaxStatus());
        }
    }

    @Nested
    @DisplayName("몽 삭제 단위 테스트")
    class DeleteMongUseCase {

        @Test
        @DisplayName("몽을 삭제 한다.")
        void deleteMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.deleteMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            DeleteMongCommand command = DeleteMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            var expected = managementUseCase.deleteMongUseCase(command);

            // assert
            assertEquals(mongId, expected.getMongId());
            assertEquals(accountId, expected.getAccountId());
            assertEquals(maxStatus, expected.getMaxStatus());
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            DeleteMongCommand command = DeleteMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.deleteMongUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            DeleteMongCommand command = DeleteMongCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.deleteMongUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 사망 단위 테스트")
    class DeadMongUseCase {

        @Test
        @DisplayName("몽을 사망 상태로 변경 한다.")
        void deadMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            DeadMongCommand command = DeadMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            var expected = managementUseCase.deadMongUseCase(command);

            // assert
            assertEquals(MongStateCode.DEAD, expected.getStateCode());
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void deadMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            DeadMongCommand command = DeadMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.deadMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태인 경우 예외가 발생 한다.")
        void deadMongWhenIsGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            DeadMongCommand command = DeadMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.deadMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            DeadMongCommand command = DeadMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.deadMongUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            DeadMongCommand command = DeadMongCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.deadMongUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 목록 조회 단위 테스트")
    class GetMongsUseCase {

        @Test
        @DisplayName("몽 목록을 조회 한다.")
        void getMongs() {
            // arrange
            final long accountId = 1L;
            final double maxStatus = 100D;
            final List<Mong> mongs = List.of(
                    MongTestUtil.getFirstLevelMong(1L, accountId, maxStatus),
                    MongTestUtil.getFirstLevelMong(2L, accountId, maxStatus),
                    MongTestUtil.getFirstLevelMong(3L, accountId, maxStatus));

            Mockito.when(mongReadPort.getMongsPort(accountId)).thenReturn(mongs);

            // act
            GetMongsCommand command = GetMongsCommand.builder()
                    .accountId(accountId)
                    .build();

            var expected = managementUseCase.getMongsUseCase(command);

            // assert
            assertIterableEquals(mongs, expected);
        }
    }

    @Nested
    @DisplayName("몽 조회 단위 테스트")
    class GetMongUseCase {

        @Test
        @DisplayName("몽을 단건 조회 한다.")
        void getMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongReadPort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act
            GetMongCommand command = GetMongCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            var expected = managementUseCase.getMongUseCase(command);

            // assert
            assertEquals(mong.getMongId(), expected.getMongId());
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongReadPort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            GetMongCommand command = GetMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.getMongUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongReadPort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            GetMongCommand command = GetMongCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.getMongUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 쓰다 듬기 단위 테스트")
    class StrokeMongUseCase{

        @Test
        @DisplayName("몽을 쓰다 듬고 지수를 증가 시킨다.")
        void strokeMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);
            final long expirationSeconds = 100;
            final long nowExpirationSeconds = 0;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getMongStrokeExpirationSecondsPort(mongId)).thenReturn(nowExpirationSeconds);
            Mockito.when(mongPersistencePort.createMongStrokeHistoryPort(mongId, Mong.getStrokeExpirationSeconds())).thenReturn(Optional.of(expirationSeconds));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            StrokeMongCommand command = StrokeMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            var expected = managementUseCase.strokeMongUseCase(command);

            // assert
            assertEquals(1, expected.getStrokeCount());
            assertTrue(expected.getExp() > 0);
        }

        @Test
        @DisplayName("쓰다 듬기 대기 시간이 남은 경우 예외가 발생 한다.")
        void strokeMongWhenIsRestExpirationSeconds() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);
            final long expirationSeconds = 100;
            final long nowExpirationSeconds = 50;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getMongStrokeExpirationSecondsPort(mongId)).thenReturn(nowExpirationSeconds);
            Mockito.when(mongPersistencePort.createMongStrokeHistoryPort(mongId, Mong.getStrokeExpirationSeconds())).thenReturn(Optional.of(expirationSeconds));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            StrokeMongCommand command = StrokeMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidStrokeMongException.class, () -> managementUseCase.strokeMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void strokeMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);
            final long expirationSeconds = 100;
            final long nowExpirationSeconds = 0;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getMongStrokeExpirationSecondsPort(mongId)).thenReturn(nowExpirationSeconds);
            Mockito.when(mongPersistencePort.createMongStrokeHistoryPort(mongId, Mong.getStrokeExpirationSeconds())).thenReturn(Optional.of(expirationSeconds));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            StrokeMongCommand command = StrokeMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.strokeMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태인 경우 예외가 발생 한다.")
        void strokeMongWhenIsGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);
            final long expirationSeconds = 100;
            final long nowExpirationSeconds = 0;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getMongStrokeExpirationSecondsPort(mongId)).thenReturn(nowExpirationSeconds);
            Mockito.when(mongPersistencePort.createMongStrokeHistoryPort(mongId, Mong.getStrokeExpirationSeconds())).thenReturn(Optional.of(expirationSeconds));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            StrokeMongCommand command = StrokeMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.strokeMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 알 상태인 경우 예외가 발생 한다.")
        void strokeMongWhenIsEgg() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);
            final long expirationSeconds = 100;
            final long nowExpirationSeconds = 0;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getMongStrokeExpirationSecondsPort(mongId)).thenReturn(nowExpirationSeconds);
            Mockito.when(mongPersistencePort.createMongStrokeHistoryPort(mongId, Mong.getStrokeExpirationSeconds())).thenReturn(Optional.of(expirationSeconds));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            StrokeMongCommand command = StrokeMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.strokeMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 수면 상태인 경우 예외가 발생 한다.")
        void strokeMongWhenIsSleeping() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, true);
            final long expirationSeconds = 100;
            final long nowExpirationSeconds = 0;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getMongStrokeExpirationSecondsPort(mongId)).thenReturn(nowExpirationSeconds);
            Mockito.when(mongPersistencePort.createMongStrokeHistoryPort(mongId, Mong.getStrokeExpirationSeconds())).thenReturn(Optional.of(expirationSeconds));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            StrokeMongCommand command = StrokeMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.strokeMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());
            // act & assert
            StrokeMongCommand command = StrokeMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.strokeMongUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            StrokeMongCommand command = StrokeMongCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.strokeMongUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 수면 단위 테스트")
    class SleepMongUseCase {

        @Test
        @DisplayName("몽을 수면 상태로 변경 한다.")
        void sleepMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, false);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));
            Mockito.when(mongSchedulerPort.createCycleTaskPort(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Optional.of(Mockito.anyLong()));

            // act
            SleepMongCommand command = SleepMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            var expected = managementUseCase.sleepMongUseCase(command);

            // assert
            assertTrue(expected.getIsSleep());
        }

        @Test
        @DisplayName("몽이 수면 상태인 경우 예외가 발생 한다.")
        void sleepMongWhenIsSleeping() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, true);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            SleepMongCommand command = SleepMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.sleepMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void sleepMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            SleepMongCommand command = SleepMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.sleepMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태인 경우 예외가 발생 한다.")
        void sleepMongWHenIsGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            SleepMongCommand command = SleepMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.sleepMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 알 상태인 경우 예외가 발생 한다.")
        void sleepMongWhenIsEgg() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            SleepMongCommand command = SleepMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.sleepMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            SleepMongCommand command = SleepMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.sleepMongUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            SleepMongCommand command = SleepMongCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.sleepMongUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 기상 단위 테스트")
    class WakeupMongUseCase{

        @Test
        @DisplayName("몽을 기상 상태로 변경 한다.")
        void wakeupMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, true);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));
            Mockito.when(mongSchedulerPort.createCycleTaskPort(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Optional.of(Mockito.anyLong()));

            // act
            WakeupMongCommand command = WakeupMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            var expected = managementUseCase.wakeUpMongUseCase(command);

            // assert
            assertFalse(expected.getIsSleep());
        }

        @Test
        @DisplayName("몽이 기상 상태인 경우 예외가 발생 한다.")
        void wakeupMongWhenIsNotSleeping() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, false);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            WakeupMongCommand command = WakeupMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.wakeUpMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void wakeupMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            WakeupMongCommand command = WakeupMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.wakeUpMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태인 경우 예외가 발생 한다.")
        void wakeupMongWHenIsGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            WakeupMongCommand command = WakeupMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.wakeUpMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 알 상태인 경우 예외가 발생 한다.")
        void wakeupMongWhenIsEgg() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            WakeupMongCommand command = WakeupMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.wakeUpMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            WakeupMongCommand command = WakeupMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.wakeUpMongUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            WakeupMongCommand command = WakeupMongCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.wakeUpMongUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 배변 처리 단위 테스트")
    class PoopCleanMongUseCase {

        @Test
        @DisplayName("몽 배변을 처리하고 지수를 증가 시킨다.")
        void poopCleanMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final int poopCount = 4;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, poopCount);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            PoopCleanMongCommand command = PoopCleanMongCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            var expected = managementUseCase.poopCleanMongUseCase(command);

            // assert
            assertTrue(expected.getExp() > 0);
            assertEquals(0, expected.getPoopCount());
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void poopCleanMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            PoopCleanMongCommand command = PoopCleanMongCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.poopCleanMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태인 경우 예외가 발생 한다.")
        void poopCleanMongWhenIsGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            PoopCleanMongCommand command = PoopCleanMongCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.poopCleanMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 알 상태인 경우 예외가 발생 한다.")
        void poopCleanWhenIsEgg() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            PoopCleanMongCommand command = PoopCleanMongCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.poopCleanMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 수면 상태인 경우 예외가 발생 한다.")
        void poopCleanMongWhenIsSleeping() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, true);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            PoopCleanMongCommand command = PoopCleanMongCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.poopCleanMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            PoopCleanMongCommand command = PoopCleanMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.poopCleanMongUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            PoopCleanMongCommand command = PoopCleanMongCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.poopCleanMongUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 진화 단위 테스트")
    class EvolutionMongUseCase {

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

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getNextLevelMongTypesPort(mong.getMongCode())).thenReturn(List.of(mongType));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            EvolutionMongCommand command = EvolutionMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            var expected = managementUseCase.evolutionMongUseCase(command);

            // assert
            assertNotEquals(MongStateCode.EVOLUTION_READY, expected.getStateCode());
            assertEquals(0D, expected.getEvolutionReward());
            assertEquals(0D, expected.getEvolutionPenalty());
            assertEquals(mongType.getMongCode(), expected.getMongCode());
            assertEquals(mongType.getMongName(), expected.getMongName());
            assertEquals(mongType.getLevel(), expected.getLevel());
            assertEquals(nextMaxStatus, expected.getMaxStatus());
            assertEquals(nextMaxStatus, expected.getStrength());
            assertEquals(nextMaxStatus, expected.getSatiety());
            assertEquals(nextMaxStatus, expected.getHealthy());
            assertEquals(nextMaxStatus, expected.getFatigue());
            assertEquals(0D, expected.getExp());
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

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getNextLevelMongTypesPort(mong.getMongCode())).thenReturn(List.of(mongType));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            EvolutionMongCommand command = EvolutionMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.evolutionMongUseCase(command));
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

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getNextLevelMongTypesPort(mong.getMongCode())).thenReturn(List.of(mongType));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            EvolutionMongCommand command = EvolutionMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.evolutionMongUseCase(command));
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

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getNextLevelMongTypesPort(mong.getMongCode())).thenReturn(List.of(mongType));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            EvolutionMongCommand command = EvolutionMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.evolutionMongUseCase(command));
        }

        @Test
        @DisplayName("진화 가능한 몽 타입이 없는 경우 예외가 발생 한다.")
        void evolutionMongWHenNotExistsMongTypes() {
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.EVOLUTION_READY);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getNextLevelMongTypesPort(mong.getMongCode())).thenReturn(Collections.emptyList());
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            EvolutionMongCommand command = EvolutionMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidEvolutionException.class, () -> managementUseCase.evolutionMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            EvolutionMongCommand command = EvolutionMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.evolutionMongUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            EvolutionMongCommand command = EvolutionMongCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.evolutionMongUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 졸업 단위 테스트")
    class GraduateMongUseCase {

        @Test
        @DisplayName("몽을 졸업 상태로 변경 한다.")
        void graduateMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.GRADUATE_READY);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            GraduateMongCommand command = GraduateMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            var expected = managementUseCase.graduateMongUseCase(command);

            // assert
            assertEquals(MongStateCode.GRADUATE, expected.getStateCode());
            assertEquals(MongStatusCode.NORMAL, expected.getStatusCode());
        }

        @Test
        @DisplayName("몽이 졸업 준비 상태가 아닌 경우 예외가 발생 한다.")
        void graduateMongWhenIsNotGraduateReady() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.NORMAL);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            GraduateMongCommand command = GraduateMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.graduateMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 사망 상태인 경우 예외가 발생 한다.")
        void graduateMongWhenIsDead() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, MongStateCode.DEAD);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            GraduateMongCommand command = GraduateMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.graduateMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 알 상태인 경우 예외가 발생 한다.")
        void graduateMongWhenIsEgg() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            GraduateMongCommand command = GraduateMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> managementUseCase.graduateMongUseCase(command));
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            GraduateMongCommand command = GraduateMongCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.graduateMongUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            GraduateMongCommand command = GraduateMongCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.graduateMongUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 페이 포인트 증가 단위 테스트")
    class IncreaseMongPayPointUseCase {

        @Test
        @DisplayName("몽의 페이 포인트를 증가 시킨다.")
        void increaseMongPayPoint() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final int payPoint = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            IncreaseMongPayPointCommand command = IncreaseMongPayPointCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .payPoint(payPoint)
                    .build();

            var expected = managementUseCase.increaseMongPayPointUseCase(command);

            // assert
            assertEquals(payPoint, expected.getPayPoint());
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            IncreaseMongPayPointCommand command = IncreaseMongPayPointCommand.builder()
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.increaseMongPayPointUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 지수 증가 단위 테스트")
    class IncreaseMongStatusUseCase {

        @Test
        @DisplayName("몽의 지수를 1 cycle 증가 시킨다.")
        void increaseMongStatus() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double status = 0;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, status, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            IncreaseMongStatusCommand command = IncreaseMongStatusCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            var expected = managementUseCase.increaseMongStatusUseCase(command);

            // assert
            assertTrue(expected.getHealthy() > status);
            assertTrue(expected.getFatigue() > status);
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());
            // act & assert
            IncreaseMongStatusCommand command = IncreaseMongStatusCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.increaseMongStatusUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            IncreaseMongStatusCommand command = IncreaseMongStatusCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.increaseMongStatusUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 지수 감소 단위 테스트")
    class DecreaseMongStatusUseCase {

        @Test
        @DisplayName("몽의 지수를 1 cycle 감소 시킨다.")
        void decreaseMongStatus() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            DecreaseMongStatusCommand command = DecreaseMongStatusCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            var expected = managementUseCase.decreaseMongStatusUseCase(command);

            // assert
            assertTrue(expected.getWeight() < maxStatus);
            assertTrue(expected.getStrength() < maxStatus);
            assertTrue(expected.getSatiety() < maxStatus);
            assertTrue(expected.getHealthy() < maxStatus);
            assertTrue(expected.getFatigue() < maxStatus);
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());
            // act & assert
            DecreaseMongStatusCommand command = DecreaseMongStatusCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.decreaseMongStatusUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            DecreaseMongStatusCommand command = DecreaseMongStatusCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.decreaseMongStatusUseCase(command));
        }
    }

    @Nested
    @DisplayName("몽 배변 수 증가 단위 테스트")
    class IncreaseMongPoopCountUseCase {

        @Test
        @DisplayName("몽 배변 수를 1 cycle 증가 시킨다.")
        void increaseMongPoopCount() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final int poopCount = 0;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus, poopCount);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            IncreaseMongPoopCountCommand command = IncreaseMongPoopCountCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            var expected = managementUseCase.increaseMongPoopCountUseCase(command);

            // assert
            assertTrue(expected.getPoopCount() > poopCount);
            assertEquals(0, expected.getEvolutionPenalty());
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

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            IncreaseMongPoopCountCommand command = IncreaseMongPoopCountCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            var expected = managementUseCase.increaseMongPoopCountUseCase(command);

            // assert
            assertEquals(poopCount, expected.getPoopCount());
            assertTrue(expected.getEvolutionPenalty() > 0);
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 예외가 발생 한다.")
        void notExistsMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            IncreaseMongPoopCountCommand command = IncreaseMongPoopCountCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> managementUseCase.increaseMongPoopCountUseCase(command));
        }

        @Test
        @DisplayName("몽의 소유자가 일치하지 않는 경우 예외가 발생 한다.")
        void forbiddenMong() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));

            // act & assert
            IncreaseMongPoopCountCommand command = IncreaseMongPoopCountCommand.builder()
                    .accountId(accountId + 1)
                    .mongId(mongId)
                    .build();

            assertThrows(ForbiddenMongException.class, () -> managementUseCase.increaseMongPoopCountUseCase(command));
        }
    }
}