package com.monglife.mongs.application.mong.port.in.admin.service;

import com.monglife.mongs.application.mong.port.in.admin.AdminMongStatsUseCase;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMongStatsVo;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongReadPort;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongSchedulerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AdminMongStatsService implements AdminMongStatsUseCase {

    private final AdminMongReadPort adminMongReadPort;

    private final AdminMongSchedulerPort adminMongSchedulerPort;

    @Override
    @Transactional
    public AdminMongStatsVo getStatsUseCase() {
        return AdminMongStatsVo.builder()
                .totalMongs(adminMongReadPort.countMongsPort())
                .todayCreated(adminMongReadPort.countMongsCreatedSincePort(LocalDate.now().atStartOfDay()))
                .countByState(adminMongReadPort.countMongsByStatePort())
                .countByStatus(adminMongReadPort.countMongsByStatusPort())
                .scheduledTasks(adminMongSchedulerPort.countScheduledPort())
                .build();
    }
}
