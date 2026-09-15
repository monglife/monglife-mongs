package com.monglife.mongs.application.battle.port.in.admin.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminBattleStatsVo {

    private final Long queueSize;

    private final Long totalMatches;

    private final Long todayMatches;

    private final Long todayBotMatches;

    private final Long processingMatches;

    @Builder
    public AdminBattleStatsVo(Long queueSize, Long totalMatches, Long todayMatches, Long todayBotMatches, Long processingMatches) {
        this.queueSize = queueSize;
        this.totalMatches = totalMatches;
        this.todayMatches = todayMatches;
        this.todayBotMatches = todayBotMatches;
        this.processingMatches = processingMatches;
    }
}
