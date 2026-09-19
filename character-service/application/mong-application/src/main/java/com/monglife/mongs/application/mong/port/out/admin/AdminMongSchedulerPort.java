package com.monglife.mongs.application.mong.port.out.admin;

import com.monglife.mongs.application.mong.port.in.admin.vo.AdminTaskVo;

import java.util.List;
import java.util.Optional;

public interface AdminMongSchedulerPort {

    List<AdminTaskVo> getTasksPort(Long mongId);

    Optional<AdminTaskVo> pauseTaskPort(Long taskId);

    Optional<AdminTaskVo> resumeTaskPort(Long taskId);

    /** 메모리에 올라가 실제로 돌고 있는 스케줄 수 */
    Long countScheduledPort();
}
