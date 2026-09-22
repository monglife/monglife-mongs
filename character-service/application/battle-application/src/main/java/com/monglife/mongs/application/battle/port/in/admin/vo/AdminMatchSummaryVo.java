package com.monglife.mongs.application.battle.port.in.admin.vo;

import com.monglife.mongs.domain.battle.enums.MatchStateCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** 매치 목록용 요약. 상세는 {@code Match} 도메인을 그대로 쓴다 */
@Getter
public class AdminMatchSummaryVo {

    private final Long matchId;

    private final Integer round;

    private final Integer maxRound;

    private final MatchStateCode stateCode;

    private final Integer playerCount;

    private final Integer botCount;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    @Builder
    public AdminMatchSummaryVo(Long matchId, Integer round, Integer maxRound, MatchStateCode stateCode, Integer playerCount, Integer botCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.matchId = matchId;
        this.round = round;
        this.maxRound = maxRound;
        this.stateCode = stateCode;
        this.playerCount = playerCount;
        this.botCount = botCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
