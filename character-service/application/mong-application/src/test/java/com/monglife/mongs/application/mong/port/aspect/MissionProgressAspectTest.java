package com.monglife.mongs.application.mong.port.aspect;

import com.monglife.mongs.application.mong.port.annotation.MissionProgress;
import com.monglife.mongs.application.mong.port.in.MissionUseCase;
import com.monglife.mongs.application.mong.port.in.command.FeedFoodCommand;
import com.monglife.mongs.application.mong.port.in.command.IncreaseMissionProgressCommand;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mong.model.RandomDraw;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MissionProgressAspectTest {

    private final MissionUseCase missionUseCase = Mockito.mock(MissionUseCase.class);
    private final MissionProgressAspect missionProgressAspect = new MissionProgressAspect(missionUseCase);

    /** 애노테이션 인스턴스를 만들려고 실제 메서드에서 읽어 온다 */
    private static class Hooked {

        @MissionProgress(value = MissionActionCode.FEED_FOOD, detailField = "foodCode")
        void feedFood() {}

        @MissionProgress(value = MissionActionCode.RANDOM_DRAW, detailField = "randomDrawCode")
        void randomDraw() {}

        @MissionProgress(MissionActionCode.STROKE)
        void stroke() {}

        @MissionProgress(value = MissionActionCode.FEED_FOOD, detailField = "존재하지않는필드")
        void broken() {}
    }

    private static MissionProgress annotationOf(String methodName) {
        try {
            return Hooked.class.getDeclaredMethod(methodName).getAnnotation(MissionProgress.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static JoinPoint joinPoint(Object... args) {
        JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
        Mockito.when(joinPoint.getArgs()).thenReturn(args);
        return joinPoint;
    }

    private IncreaseMissionProgressCommand captured() {
        ArgumentCaptor<IncreaseMissionProgressCommand> captor = ArgumentCaptor.forClass(IncreaseMissionProgressCommand.class);
        Mockito.verify(missionUseCase).increaseMissionProgressUseCase(captor.capture());
        return captor.getValue();
    }

    @Nested
    @DisplayName("미션 진행도 훅 단위 테스트")
    class AfterReturning {

        @Test
        @DisplayName("커맨드에서 계정 ID 와 집계 대상 코드를 읽는다.")
        void readFromCommand() {
            // arrange
            FeedFoodCommand command = FeedFoodCommand.builder().accountId(1L).mongId(1L).foodCode("FD000").build();

            // act
            missionProgressAspect.afterReturning(joinPoint(command), annotationOf("feedFood"), null);

            // assert
            IncreaseMissionProgressCommand expected = captured();
            assertEquals(1L, expected.getAccountId());
            assertEquals(MissionActionCode.FEED_FOOD, expected.getActionCode());
            assertEquals("FD000", expected.getDetailCode());
        }

        @Test
        @DisplayName("커맨드에 없으면 반환값에서 집계 대상 코드를 읽는다.")
        void readFromReturnValue() {
            // arrange - 뽑기 결과는 커맨드가 아니라 반환값에만 있다
            com.monglife.mongs.application.mong.port.in.command.RandomDrawCommand command =
                    com.monglife.mongs.application.mong.port.in.command.RandomDrawCommand.builder()
                            .accountId(1L).mongId(1L).build();
            RandomDraw randomDraw = RandomDraw.builder().randomDrawCode("FD010").build();

            // act
            missionProgressAspect.afterReturning(joinPoint(command), annotationOf("randomDraw"), randomDraw);

            // assert
            assertEquals("FD010", captured().getDetailCode());
        }

        @Test
        @DisplayName("집계 대상 필드를 지정하지 않으면 대상 코드 없이 올린다.")
        void withoutDetailField() {
            // arrange
            com.monglife.mongs.application.mong.port.in.command.StrokeMongCommand command =
                    com.monglife.mongs.application.mong.port.in.command.StrokeMongCommand.builder()
                            .accountId(1L).mongId(1L).build();

            // act
            missionProgressAspect.afterReturning(joinPoint(command), annotationOf("stroke"), null);

            // assert
            assertNull(captured().getDetailCode());
        }

        @Test
        @DisplayName("계정 ID 를 못 읽으면 집계를 건너뛴다.")
        void skipWhenAccountIdIsMissing() {
            // arrange - 커맨드가 없는 경우
            // act
            missionProgressAspect.afterReturning(joinPoint(), annotationOf("stroke"), null);

            // assert - 여기서 터지면 애노테이션 한 줄이 플레이 기능을 막는다
            Mockito.verify(missionUseCase, Mockito.never()).increaseMissionProgressUseCase(Mockito.any());
        }

        @Test
        @DisplayName("없는 필드를 가리켜도 집계 대상만 비우고 넘어가지 않는다.")
        void skipWhenDetailFieldIsWrong() {
            // arrange - 필드 이름 오타는 설정 실수다. DB 를 건드리기 전이라 트랜잭션에 영향이 없다
            FeedFoodCommand command = FeedFoodCommand.builder().accountId(1L).mongId(1L).foodCode("FD000").build();

            // act
            missionProgressAspect.afterReturning(joinPoint(command), annotationOf("broken"), null);

            // assert - 대상 코드는 못 찾지만 COUNT 미션은 올라야 한다
            assertNull(captured().getDetailCode());
        }
    }
}
