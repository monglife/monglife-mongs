package com.monglife.mongs.application.mong.port.aspect;

import com.monglife.mongs.application.mong.port.annotation.MissionProgress;
import com.monglife.mongs.application.mong.port.in.MissionUseCase;
import com.monglife.mongs.application.mong.port.in.command.IncreaseMissionProgressCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * {@code @MissionProgress} 가 붙은 유스케이스가 성공하면 미션 진행도를 올린다.
 *
 * <p>{@code PublishMongPortAspect} 와 같은 모양이다. 진행도 집계는 호출한 유스케이스의
 * 트랜잭션에 함께 묶인다(REQUIRES_NEW 를 쓰지 않는다) - 커넥션 풀이 6이라
 * 요청 하나가 커넥션 두 개를 잡으면 동시 요청 몇 개만으로 서로를 기다리게 된다.
 * 그래서 "밥은 줬는데 진행도만 사라지는" 상태도 생기지 않는다.
 *
 * <p>대신 실제 경합이 나는 두 삽입(사용자 미션 적재, DISTINCT 대상 적재)은 어댑터에서
 * INSERT IGNORE 로 처리해 예외 자체가 나지 않게 해 두었다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MissionProgressAspect {

    private final MissionUseCase missionUseCase;

    @AfterReturning(value = "@annotation(missionProgress)", returning = "returnValue")
    public void afterReturning(JoinPoint joinPoint, MissionProgress missionProgress, Object returnValue) {

        Object command = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;

        Long accountId;
        String detailCode;

        try {
            accountId = (Long) this.readField(command, "accountId");

            detailCode = missionProgress.detailField().isBlank()
                    ? null
                    : (String) this.firstNonNull(
                            this.readField(command, missionProgress.detailField()),
                            this.readField(returnValue, missionProgress.detailField()));

        } catch (Exception exception) {
            // 필드 이름 오타 같은 설정 실수다. 여기서 터뜨리면 애노테이션 한 줄이 플레이 기능을 막는다.
            // DB 를 건드리기 전이라 트랜잭션에도 영향이 없다.
            log.error("미션 진행도 대상 추출 실패 - 집계를 건너뛴다. action={}", missionProgress.value(), exception);
            return;
        }

        if (accountId == null) {
            log.error("미션 진행도에 계정 ID 가 없다 - 집계를 건너뛴다. action={}", missionProgress.value());
            return;
        }

        missionUseCase.increaseMissionProgressUseCase(IncreaseMissionProgressCommand.builder()
                .accountId(accountId)
                .actionCode(missionProgress.value())
                .detailCode(detailCode)
                .build());
    }

    /**
     * 객체에서 이름으로 필드 값 추출. 상속 계층을 거슬러 올라간다
     */
    private Object readField(Object target, String name) throws IllegalAccessException {

        for (Class<?> type = target == null ? null : target.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    field.setAccessible(true);
                    return field.get(target);
                }
            }
        }

        return null;
    }

    private Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }
}
