package com.monglife.mongs.adapter.out.mong.schedule.service.admin;

import com.monglife.mongs.adapter.out.mong.schedule.entity.TaskEntity;
import com.monglife.mongs.adapter.out.mong.schedule.entity.TaskScheduleEntity;
import com.monglife.mongs.adapter.out.mong.schedule.enums.TaskStateCode;
import com.monglife.mongs.adapter.out.mong.schedule.repository.TaskRepository;
import com.monglife.mongs.adapter.out.mong.schedule.repository.TaskScheduleRepository;
import com.monglife.mongs.adapter.out.mong.schedule.enums.TaskTypeCode;
import com.monglife.mongs.application.mong.port.enums.SchedulerType;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminTaskVo;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongSchedulerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 관리자 스케줄 조회·일시중지·재시작. {@code TaskService} 와 같은 executor·레포지토리를 쓴다.
 */
@Service
public class AdminTaskService implements AdminMongSchedulerPort {

    private static final String APP_PACKAGE_NAME = "com.monglife.mongs";

    private final TaskRepository taskRepository;

    private final TaskScheduleRepository taskScheduleRepository;

    private final ApplicationEventPublisher publisher;

    private final ScheduledExecutorService executor;

    public AdminTaskService(
            @Autowired TaskRepository taskRepository,
            @Autowired TaskScheduleRepository taskScheduleRepository,
            @Autowired ApplicationEventPublisher publisher,
            @Qualifier("taskScheduledExecutorService") ScheduledExecutorService executor
    ) {
        this.taskRepository = taskRepository;
        this.taskScheduleRepository = taskScheduleRepository;
        this.publisher = publisher;
        this.executor = executor;
    }

    @Override
    @Transactional
    public List<AdminTaskVo> getTasksPort(Long mongId) {
        return taskRepository.findAllByAppPackageNameAndMongIdWithLock(APP_PACKAGE_NAME, mongId).stream()
                .map(this::toVo)
                .toList();
    }

    @Override
    @Transactional
    public Optional<AdminTaskVo> pauseTaskPort(Long taskId) {
        return taskRepository.findByTaskIdWithLock(taskId).map(taskEntity -> {
            taskEntity.pause();
            taskScheduleRepository.findByTaskId(taskEntity.getTaskId()).ifPresent(TaskScheduleEntity::stop);
            taskScheduleRepository.deleteByTaskId(taskEntity.getTaskId());
            return toVo(taskEntity);
        });
    }

    @Override
    @Transactional
    public Optional<AdminTaskVo> resumeTaskPort(Long taskId) {
        return taskRepository.findByTaskIdWithLock(taskId).map(taskEntity -> {
            taskEntity.resume();
            if (TaskStateCode.PROCESSING.equals(taskEntity.getStateCode())) {
                taskScheduleRepository.save(TaskScheduleEntity.of(taskEntity))
                        .start(executor, () -> publisher.publishEvent(taskEntity));
            }
            return toVo(taskEntity);
        });
    }

    /**
     * 삭제. 타이머를 먼저 끊고 행을 지운다.
     *
     * <p>{@code TaskService.deleteTaskPort} 와 같은 순서다 - 맵에서 빼기 전에 cancel 해야
     * 취소되지 않은 퓨처가 남아 행 없이 발화한다.
     */
    @Override
    @Transactional
    public Optional<AdminTaskVo> deleteTaskPort(Long taskId) {

        return taskRepository.findByTaskIdWithLock(taskId).map(taskEntity -> {
            AdminTaskVo vo = this.toVo(taskEntity);
            taskScheduleRepository.findByTaskId(taskId).ifPresent(TaskScheduleEntity::stop);
            taskScheduleRepository.deleteByTaskId(taskId);
            taskRepository.delete(taskEntity);
            return vo;
        });
    }

    @Override
    @Transactional
    public Boolean isExistsTaskPort(Long mongId, SchedulerType schedulerType) {
        return taskRepository.findByAppPackageNameAndMongIdAndSchedulerTypeCode(APP_PACKAGE_NAME, mongId, schedulerType.getCode()).isPresent();
    }

    /**
     * 등록.
     *
     * <p>주기 방식은 {@code SchedulerType.getKind()} 가 정한다 - 만료 초로 추론하면
     * 알 부화(300초, 일회성)와 지수 감소(900초, 반복)가 갈라지지 않는다.
     *
     * <p>호출 전에 {@code isExistsTaskPort} 로 중복을 걸러야 한다. 기존 행이 있으면
     * 그 행을 재사용하는데, 일시중지된 행이면 {@code expiredAt} 이 null 이라 start 에서 터진다.
     */
    @Override
    @Transactional
    public Optional<AdminTaskVo> createTaskPort(Long mongId, Long accountId, SchedulerType schedulerType, LocalTime fixTime) {

        TaskEntity taskEntity = switch (schedulerType.getKind()) {
            case FIXED_TIME_CYCLE -> new TaskEntity(APP_PACKAGE_NAME, mongId, accountId, schedulerType.getCode(),
                    TaskTypeCode.FIX_TIME_CYCLE, fixTime);
            case CYCLE -> new TaskEntity(APP_PACKAGE_NAME, mongId, accountId, schedulerType.getCode(),
                    TaskTypeCode.NONE_FIX_TIME_CYCLE, schedulerType.getExpiration());
            case ONCE -> new TaskEntity(APP_PACKAGE_NAME, mongId, accountId, schedulerType.getCode(),
                    TaskTypeCode.NONE_FIX_TIME, schedulerType.getExpiration());
        };

        final TaskEntity saved = taskRepository.save(taskEntity);

        taskScheduleRepository.save(TaskScheduleEntity.of(saved))
                .start(executor, () -> publisher.publishEvent(saved));

        return Optional.of(this.toVo(saved));
    }

    @Override
    public Long countScheduledPort() {
        return taskScheduleRepository.count().longValue();
    }

    private AdminTaskVo toVo(TaskEntity taskEntity) {
        return AdminTaskVo.builder()
                .taskId(taskEntity.getTaskId())
                .mongId(taskEntity.getMongId())
                .accountId(taskEntity.getAccountId())
                .schedulerTypeCode(taskEntity.getSchedulerTypeCode())
                .stateCode(taskEntity.getStateCode() == null ? null : taskEntity.getStateCode().name())
                .typeCode(taskEntity.getTypeCode() == null ? null : taskEntity.getTypeCode().name())
                .expirationSeconds(taskEntity.getExpirationSeconds())
                .restExpirationSeconds(taskEntity.getRestExpirationSeconds())
                .expiredAt(taskEntity.getExpiredAt())
                .fixTime(taskEntity.getFixTime())
                .isScheduled(taskScheduleRepository.findByTaskId(taskEntity.getTaskId()).isPresent())
                .createdAt(taskEntity.getCreatedAt())
                .updatedAt(taskEntity.getUpdatedAt())
                .build();
    }
}
