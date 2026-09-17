package com.monglife.mongs.application.mong.port.annotation;

import com.monglife.mongs.domain.mission.enums.MissionActionCode;

import java.lang.annotation.*;

/**
 * 이 유스케이스가 성공하면 미션 진행도를 올린다.
 *
 * <p>{@code @PublishMongPort} 와 같은 방식이다. 유스케이스 본문에 집계 코드를 끼워 넣지 않고
 * 애스펙트가 반환 직후에 처리한다.
 *
 * <p>누적 수치(ACCUMULATE)는 반환값에서 끌어낼 수 없는 경우가 많아
 * 해당 유스케이스가 {@code MissionUseCase} 를 직접 호출한다. 이 애노테이션은 COUNT·DISTINCT 용이다.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MissionProgress {

    /** 올릴 액션 */
    MissionActionCode value();

    /**
     * DISTINCT 미션의 집계 대상 코드를 읽어올 필드 이름.
     *
     * <p>커맨드(첫 인자)에서 먼저 찾고, 없으면 반환값에서 찾는다.
     * 비워 두면 DISTINCT 집계 없이 COUNT 만 오른다.
     */
    String detailField() default "";
}
