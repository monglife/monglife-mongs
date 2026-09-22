package com.monglife.mongs.application.mong.port.in.admin.vo;

import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class AdminMongStatsVo {

    private final Long totalMongs;

    private final Long todayCreated;

    private final Map<MongStateCode, Long> countByState;

    private final Map<MongStatusCode, Long> countByStatus;

    private final Long scheduledTasks;

    @Builder
    public AdminMongStatsVo(Long totalMongs, Long todayCreated, Map<MongStateCode, Long> countByState, Map<MongStatusCode, Long> countByStatus, Long scheduledTasks) {
        this.totalMongs = totalMongs;
        this.todayCreated = todayCreated;
        this.countByState = countByState;
        this.countByStatus = countByStatus;
        this.scheduledTasks = scheduledTasks;
    }
}
