package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monglife.mongs.application.battle.port.in.admin.vo.AdminMatchSummaryVo;

import java.time.LocalDateTime;

public record AdminMatchSummaryResponseDto(Long matchId, Integer round, Integer maxRound, String stateCode, Integer playerCount, Integer botCount, @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul") LocalDateTime createdAt,
                                                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul") LocalDateTime updatedAt) {

    public static AdminMatchSummaryResponseDto of(AdminMatchSummaryVo vo) {
        return new AdminMatchSummaryResponseDto(vo.getMatchId(), vo.getRound(), vo.getMaxRound(), vo.getStateCode() == null ? null : vo.getStateCode().name(), vo.getPlayerCount(), vo.getBotCount(), vo.getCreatedAt(), vo.getUpdatedAt());
    }
}
