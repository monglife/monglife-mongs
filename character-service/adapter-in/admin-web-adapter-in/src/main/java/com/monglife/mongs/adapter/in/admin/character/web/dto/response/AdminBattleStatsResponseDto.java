package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.monglife.mongs.application.battle.port.in.admin.vo.AdminBattleStatsVo;

public record AdminBattleStatsResponseDto(Long queueSize, Long totalMatches, Long todayMatches, Long todayBotMatches, Long processingMatches) {

    public static AdminBattleStatsResponseDto of(AdminBattleStatsVo vo) {
        return new AdminBattleStatsResponseDto(vo.getQueueSize(), vo.getTotalMatches(), vo.getTodayMatches(), vo.getTodayBotMatches(), vo.getProcessingMatches());
    }
}
