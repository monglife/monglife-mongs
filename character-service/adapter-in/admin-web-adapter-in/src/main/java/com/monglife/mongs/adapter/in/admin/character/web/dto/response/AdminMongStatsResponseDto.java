package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMongStatsVo;
import lombok.Builder;

import java.util.LinkedHashMap;
import java.util.Map;

@Builder
public record AdminMongStatsResponseDto(
        Long totalMongs,
        Long todayCreated,
        Map<String, Long> countByState,
        Map<String, Long> countByStatus,
        Long scheduledTasks
) {
    public static AdminMongStatsResponseDto of(AdminMongStatsVo vo) {
        Map<String, Long> byState = new LinkedHashMap<>();
        vo.getCountByState().forEach((code, count) -> byState.put(code.name(), count));
        Map<String, Long> byStatus = new LinkedHashMap<>();
        vo.getCountByStatus().forEach((code, count) -> byStatus.put(code.name(), count));
        return AdminMongStatsResponseDto.builder()
                .totalMongs(vo.getTotalMongs())
                .todayCreated(vo.getTodayCreated())
                .countByState(byState)
                .countByStatus(byStatus)
                .scheduledTasks(vo.getScheduledTasks())
                .build();
    }
}
