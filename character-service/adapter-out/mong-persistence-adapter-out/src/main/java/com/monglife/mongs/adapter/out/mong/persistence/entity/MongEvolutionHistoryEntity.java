package com.monglife.mongs.adapter.out.mong.persistence.entity;

import com.monglife.mongs.domain.mong.model.MongEvolutionHistory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners({ AuditingEntityListener.class })
@Table(name = "mongs_mong_evolution_history")
public class MongEvolutionHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mong_evolution_history_id")
    private Long mongEvolutionHistoryId;

    @Column(name = "mong_code")
    private String mongCode;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "evolution_score")
    private Double evolutionScore;

    @Builder
    public MongEvolutionHistoryEntity(Long mongEvolutionHistoryId, String mongCode, Long accountId, Double evolutionScore) {
        this.mongEvolutionHistoryId = mongEvolutionHistoryId;
        this.mongCode = mongCode;
        this.accountId = accountId;
        this.evolutionScore = evolutionScore;
    }

    /**
     * @param mongName 몽 이름. 이 표는 코드만 들고 있어 호출 측이 찾아 넣는다.
     *                 마스터에서 지워진 코드면 null 이 온다 - 이력은 그대로 남기고 이름만 비운다.
     */
    public MongEvolutionHistory toDomain(String mongName) {
        return MongEvolutionHistory.builder()
                .mongEvolutionHistoryId(mongEvolutionHistoryId)
                .mongCode(mongCode)
                .mongName(mongName)
                .accountId(accountId)
                .evolutionScore(evolutionScore)
                .build();
    }
}
