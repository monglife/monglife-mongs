package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.monglife.mongs.domain.mong.model.MongEvolutionHistory;

public record AdminEvolutionHistoryResponseDto(Long mongEvolutionHistoryId, Long accountId, String mongCode, Double evolutionScore) {

    public static AdminEvolutionHistoryResponseDto of(MongEvolutionHistory history) {
        return new AdminEvolutionHistoryResponseDto(history.getMongEvolutionHistoryId(), history.getAccountId(), history.getMongCode(), history.getEvolutionScore());
    }
}
