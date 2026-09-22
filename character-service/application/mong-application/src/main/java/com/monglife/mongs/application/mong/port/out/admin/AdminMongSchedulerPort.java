package com.monglife.mongs.application.mong.port.out.admin;

import com.monglife.mongs.application.mong.port.enums.SchedulerType;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminTaskVo;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AdminMongSchedulerPort {

    List<AdminTaskVo> getTasksPort(Long mongId);

    Optional<AdminTaskVo> pauseTaskPort(Long taskId);

    Optional<AdminTaskVo> resumeTaskPort(Long taskId);

    /**
     * 스케줄 삭제. 일시중지와 달리 행까지 지운다 - 다시 걸려면 등록해야 한다.
     *
     * <p>돌고 있던 타이머도 함께 취소한다. 취소하지 않으면 행이 없는 채로 발화해
     * 팬텀 이벤트가 나간다.
     */
    Optional<AdminTaskVo> deleteTaskPort(Long taskId);

    /** 그 몽에 이 타입의 스케줄이 이미 있는지 */
    Boolean isExistsTaskPort(Long mongId, SchedulerType schedulerType);

    /**
     * 스케줄 등록.
     *
     * <p>{@code fixTime} 은 SLEEP/WAKEUP 처럼 고정 시각 반복인 타입에만 쓴다. 나머지는 무시된다.
     */
    Optional<AdminTaskVo> createTaskPort(Long mongId, Long accountId, SchedulerType schedulerType, LocalTime fixTime);

    /** 메모리에 올라가 실제로 돌고 있는 스케줄 수 */
    Long countScheduledPort();
}
