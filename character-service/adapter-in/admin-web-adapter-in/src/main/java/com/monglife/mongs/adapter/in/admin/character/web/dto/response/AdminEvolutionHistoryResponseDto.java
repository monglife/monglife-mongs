package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.monglife.mongs.domain.mong.model.MongEvolutionHistory;

/** mongName 은 마스터에서 지워진 코드면 null 이다 */
public record AdminEvolutionHistoryResponseDto(Long mongEvolutionHistoryId, Long accountId, String mongCode, String mongName, Double evolutionScore) {

    public static AdminEvolutionHistoryResponseDto of(MongEvolutionHistory history) {
        return new AdminEvolutionHistoryResponseDto(
                history.getMongEvolutionHistoryId(),
                history.getAccountId(),
                history.getMongCode(),
                history.getMongName(),
                history.getEvolutionScore());
    }
}
