package com.monglife.mongs.domain.mong.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MongEvolutionHistory {

    private final Long mongEvolutionHistoryId;

    private final String mongCode;

    /** 표시용 이름. 코드만으로는 어떤 몽인지 알아보기 어렵다 */
    private final String mongName;

    private final Long accountId;

    private final Double evolutionScore;

    @Builder
    public MongEvolutionHistory(Long mongEvolutionHistoryId, String mongCode, String mongName, Long accountId, Double evolutionScore) {
        this.mongEvolutionHistoryId = mongEvolutionHistoryId;
        this.mongCode = mongCode;
        this.mongName = mongName;
        this.accountId = accountId;
        this.evolutionScore = evolutionScore;
    }
}
