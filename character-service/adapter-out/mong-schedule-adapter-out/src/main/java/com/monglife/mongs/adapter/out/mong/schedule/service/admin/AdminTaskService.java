package com.monglife.mongs.adapter.out.mong.schedule.service.admin;

import com.monglife.mongs.adapter.out.mong.schedule.entity.TaskEntity;
import com.monglife.mongs.adapter.out.mong.schedule.entity.TaskScheduleEntity;
import com.monglife.mongs.adapter.out.mong.schedule.enums.TaskStateCode;
import com.monglife.mongs.adapter.out.mong.schedule.repository.TaskRepository;
import com.monglife.mongs.adapter.out.mong.schedule.repository.TaskScheduleRepository;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminTaskVo;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongSchedulerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
