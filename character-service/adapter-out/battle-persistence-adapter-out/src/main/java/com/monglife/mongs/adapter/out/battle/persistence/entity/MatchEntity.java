package com.monglife.mongs.adapter.out.battle.persistence.entity;

import com.monglife.module.common.jpa.entity.BaseTimeEntity;
import com.monglife.mongs.domain.battle.enums.MatchStateCode;
import com.monglife.mongs.domain.battle.model.Match;
import com.monglife.mongs.domain.battle.model.MatchPick;
import com.monglife.mongs.domain.battle.model.MatchPlayer;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners({ AuditingEntityListener.class })
@Table(
        name = "mongs_match",
        // 입장 기한 스위퍼가 5초마다 (state_code, created_at) 으로 훑는다. 끝난 매치는 지우지 않아
        // 표가 단조 증가하므로 인덱스가 없으면 그 주기마다 풀스캔이 된다.
        // stg/prd 는 hbm2ddl.auto 가 none 이라 이 선언만으로는 안 생긴다 - 마이그레이션도 함께 돌린다.
        indexes = { @Index(name = "idx_match_state_created", columnList = "state_code, created_at") }
)
public class MatchEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "max_round")
    private Integer maxRound;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "match_id")
    private List<MatchPlayerEntity> matchPlayers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "match_id")
    private List<MatchPickEntity> matchPicks = new ArrayList<>();

    @Column(name = "round")
    private Integer round;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_code")
    private MatchStateCode stateCode;

    @Builder
    public MatchEntity(Long matchId, Integer maxRound, List<MatchPlayerEntity> matchPlayers, List<MatchPickEntity> matchPicks, Integer round, MatchStateCode stateCode) {
        this.matchId = matchId;
        this.maxRound = maxRound;
        this.matchPlayers = matchPlayers;
        this.matchPicks = matchPicks;
        this.round = round;
        this.stateCode = stateCode;
    }

    public void update(Match match) {
        match.getMatchPlayers().forEach(matchPlayer -> {
            if (matchPlayer.getPlayerId() == null) {
                this.matchPlayers.add(MatchPlayerEntity.builder()
                        .playerId(matchPlayer.getPlayerId())
                        .deviceId(matchPlayer.getDeviceId())
                        .accountId(matchPlayer.getAccountId())
                        .mongId(matchPlayer.getMongId())
                        .mongCode(matchPlayer.getMongCode())
                        .mongName(matchPlayer.getMongName())
                        .name(matchPlayer.getName())
                        .attack(matchPlayer.getAttack())
                        .heal(matchPlayer.getHeal())
                        .defence(matchPlayer.getDefence())
                        .isBot(matchPlayer.getIsBot())
                        .hp(matchPlayer.getHp())
                        .isEnter(matchPlayer.getIsEnter())
                        .enteredAt(matchPlayer.getEnteredAt())
                        .exitedAt(matchPlayer.getExitedAt())
                        .build());
            } else {
                this.getMatchPlayerEntity(matchPlayer.getPlayerId())
                        .ifPresent(matchPlayerEntity -> matchPlayerEntity.update(matchPlayer));
            }
        });

        match.getMatchPicks().forEach(matchPick -> {
            if (matchPick.getPickId() == null) {
                this.matchPicks.add(MatchPickEntity.builder()
                        .playerId(matchPick.getMatchPlayer().getPlayerId())
                        .targetPlayerId(matchPick.getTargetMatchPlayer().getPlayerId())
                        .round(matchPick.getRound())
                        .pickCode(matchPick.getPickCode())
                        .pickValue(matchPick.getPickValue())
                        .build());
            } else {
                this.getMatchPickEntity(matchPick.getPickId())
                        .ifPresent(matchPickEntity -> matchPickEntity.update(matchPick));
            }
        });

        this.maxRound = match.getMaxRound();
        this.round = match.getRound();
        this.stateCode = match.getStateCode();
    }

    public Match toDomain() {
        List<MatchPlayer> matchPlayers = this.matchPlayers.stream()
                .map(MatchPlayerEntity::toDomain)
                .collect(Collectors.toList());

        List<MatchPick> matchPicks = this.matchPicks.stream()
//                .filter(matchPickEntity -> this.round.equals(matchPickEntity.getRound()))
                .map(matchPickEntity -> {
                    var matchPlayer = matchPlayers.stream()
                            .filter(mp -> mp.getPlayerId().equals(matchPickEntity.getPlayerId()))
                            .findFirst()
                            .orElse(null);

                    var targetMatchPlayer = matchPlayers.stream()
                            .filter(mp -> mp.getPlayerId().equals(matchPickEntity.getTargetPlayerId()))
                            .findFirst()
                            .orElse(null);

                    return matchPickEntity.toDomain(matchPlayer, targetMatchPlayer);

                })
                .collect(Collectors.toList());

        Match match = Match.builder()
                .matchId(this.matchId)
                .round(this.round)
                .maxRound(this.maxRound)
                .stateCode(this.stateCode)
                .matchPlayers(matchPlayers)
                .matchPicks(matchPicks)
                .build();

        return match;
    }

    private Optional<MatchPlayerEntity> getMatchPlayerEntity(String playerId) {
        return this.matchPlayers.stream()
                .filter(matchPlayer -> playerId.equals(matchPlayer.getPlayerId()))
                .findFirst();
    }

    private Optional<MatchPickEntity> getMatchPickEntity(Long pickId) {
        return this.matchPicks.stream()
                .filter(matchPick -> pickId.equals(matchPick.getPickId()))
                .findFirst();
    }
}
