package com.monglife.mongs.application.mong.port.in.service;

import com.monglife.mongs.application.mong.port.exception.NotExistsMongException;
import com.monglife.mongs.application.mong.port.exception.NotExistsTrainingTypeException;
import com.monglife.mongs.application.mong.port.in.ActivityUseCase;
import com.monglife.mongs.application.mong.port.in.MissionUseCase;
import com.monglife.mongs.application.mong.port.in.command.GetTrainingTypeCommand;
import com.monglife.mongs.application.mong.port.in.command.TrainingEndCommand;
import com.monglife.mongs.application.mong.port.in.utils.MongTestUtil;
import com.monglife.mongs.application.mong.port.out.MongPersistencePort;
import com.monglife.mongs.application.mong.port.out.MongReadPort;
import com.monglife.mongs.domain.mong.model.Mong;
import com.monglife.mongs.domain.mong.model.TrainingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ActivityServiceTest {

    private final MongPersistencePort mongPersistencePort = Mockito.mock(MongPersistencePort.class);
    private final MongReadPort mongReadPort = Mockito.mock(MongReadPort.class);
    private final MissionUseCase missionUseCase = Mockito.mock(MissionUseCase.class);
    private final ActivityUseCase activityUseCase = new ActivityService(mongPersistencePort, mongReadPort, missionUseCase);

    @Nested
    @DisplayName("훈련 타입 목록 조회 단위 테스트")
    class GetTrainingTypesUseCase {

        @Test
        @DisplayName("훈련 타입 정보 목록을 조회 한다.")
        void getTrainingTypes() {
            // arrange
            final List<TrainingType> trainingTypes = List.of(
                    new TrainingType(1L, "TEST-TRAINING-TYPE-CODE", "테스트 훈련 타입", 10, 100, 60, 1D, 2D, 3D, 4D, 5D),
                    new TrainingType(1L, "TEST-TRAINING-TYPE-CODE", "테스트 훈련 타입", 10, 100, 60, 1D, 2D, 3D, 4D, 5D)
            );

            Mockito.when(mongReadPort.getTrainingTypesPort()).thenReturn(trainingTypes);

            // act
            var expected = activityUseCase.getTrainingTypesUseCase();

            // assert
            assertEquals(expected.size(), trainingTypes.size());
        }
    }

    @Nested
    @DisplayName("훈련 타입 조회 단위 테스트")
    class GetTrainingTypeUseCase {

        @Test
        @DisplayName("훈련 타입 정보를 조회 한다.")
        void getTrainingType() {
            // arrange
            final String trainingCode = "TEST-TRAINING-TYPE-CODE";
            final TrainingType trainingType = new TrainingType(1L, trainingCode, "테스트 훈련 타입", 10, 100, 60, 1D, 2D, 3D, 4D, 5D);

            Mockito.when(mongReadPort.getTrainingTypePort(trainingCode)).thenReturn(Optional.of(trainingType));

            // act
            GetTrainingTypeCommand command = GetTrainingTypeCommand.builder()
                    .trainingCode(trainingCode)
                    .build();

            TrainingType expected = activityUseCase.getTrainingTypeUseCase(command);

            // assert
            assertEquals(trainingCode, expected.getTrainingCode());
        }

        @Test
        @DisplayName("훈련 타입이 없는 경우 예외가 발생 한다.")
        void getTrainingTypeWhenNotExistsTrainingType() {
            // arrange
            final String trainingCode = "TEST-TRAINING-TYPE-CODE";

            Mockito.when(mongReadPort.getTrainingTypePort(trainingCode)).thenReturn(Optional.empty());

            // act & assert
            GetTrainingTypeCommand command = GetTrainingTypeCommand.builder()
                    .trainingCode(trainingCode)
                    .build();

            assertThrows(NotExistsTrainingTypeException.class, () -> activityUseCase.getTrainingTypeUseCase(command));
        }
    }

    @Nested
    @DisplayName("훈련 완료 단위 테스트")
    class TrainingEndUseCase {

        @Test
        @DisplayName("훈련을 완료하고 스코어를 달성시 보상을 받는다")
        void trainingEnd() {
            // arrange
            final String trainingCode = "TEST-TRAINING-TYPE-CODE";
            final int score = 100;
            final int payPoint = 10;
            final double status = 10D;
            final TrainingType trainingType = new TrainingType(1L, trainingCode, "테스트 훈련 타입", payPoint, score, 60, status, -status, -status, -status, -status);
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            Mockito.when(mongReadPort.getTrainingTypePort(trainingCode)).thenReturn(Optional.of(trainingType));
            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            TrainingEndCommand command = TrainingEndCommand.builder()
                    .accountId(accountId)
                    .trainingCode(trainingCode)
                    .mongId(mongId)
                    .score(score)
                    .build();

            var expected = activityUseCase.trainingEndUseCase(command);

            // assert
            assertEquals(status, expected.getMong().getExp());
            assertTrue(expected.getIsSuccess());
            assertEquals(trainingType.getPayPoint(), expected.getRewardPayPoint());
            assertEquals(score, expected.getScore());
            assertEquals(maxStatus - status, expected.getMong().getStrength());
            assertEquals(maxStatus - status, expected.getMong().getSatiety());
            assertEquals(maxStatus - status, expected.getMong().getFatigue());
            assertEquals(maxStatus - status, expected.getMong().getWeight());
            assertEquals(1, expected.getMong().getTrainingCount());
            assertEquals(payPoint, expected.getMong().getPayPoint());
        }

        @Test
        @DisplayName("훈련 타입이 없는 경우 예외가 발생 한다.")
        void trainingEndWhenNotExistsTrainingType() {
            // arrange
            final String trainingCode = "TEST-TRAINING-TYPE-CODE";
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100D;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            Mockito.when(mongReadPort.getTrainingTypePort(trainingCode)).thenReturn(Optional.empty());
            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act & assert
            TrainingEndCommand command = TrainingEndCommand.builder()
                    .accountId(accountId)
                    .trainingCode(trainingCode)
                    .mongId(mongId)
                    .score(0)
                    .build();

            assertThrows(NotExistsTrainingTypeException.class, () -> activityUseCase.trainingEndUseCase(command));
        }

        @Test
        @DisplayName("몽이 없는 경우 예외가 발생 한다.")
        void trainingEndWhenNotExistsMong() {
            // arrange
            final String trainingCode = "TEST-TRAINING-TYPE-CODE";
            final int score = 100;
            final int payPoint = 10;
            final double status = 10D;
            final TrainingType trainingType = new TrainingType(1L, trainingCode, "테스트 훈련 타입", payPoint, score, 60, status, -status, -status, -status, -status);
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongReadPort.getTrainingTypePort(trainingCode)).thenReturn(Optional.of(trainingType));
            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            TrainingEndCommand command = TrainingEndCommand.builder()
                    .accountId(accountId)
                    .trainingCode(trainingCode)
                    .mongId(mongId)
                    .score(0)
                    .build();

            assertThrows(NotExistsMongException.class, () -> activityUseCase.trainingEndUseCase(command));
        }
    }
}