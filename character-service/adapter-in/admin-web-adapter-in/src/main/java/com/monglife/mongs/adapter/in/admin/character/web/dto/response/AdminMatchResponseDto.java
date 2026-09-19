package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monglife.mongs.domain.battle.model.Match;
import com.monglife.mongs.domain.battle.model.MatchPick;
import com.monglife.mongs.domain.battle.model.MatchPlayer;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AdminMatchResponseDto(
        Long matchId,
        Integer round,
        Integer maxRound,
        String stateCode,
        List<PlayerDto> players,
        List<PickDto> picks
) {
    @Builder
    public record PlayerDto(String playerId, String deviceId, Long accountId, Long mongId, String mongCode, String mongName, String name,
                            Double attack, Double heal, Double defence, Boolean isBot, Double hp, Boolean isEnter,
                            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul") LocalDateTime enteredAt,
                            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul") LocalDateTime exitedAt) {
        public static PlayerDto of(MatchPlayer p) {
            return PlayerDto.builder()
                    .playerId(p.getPlayerId()).deviceId(p.getDeviceId()).accountId(p.getAccountId()).mongId(p.getMongId())
                    .mongCode(p.getMongCode()).mongName(p.getMongName()).name(p.getName())
                    .attack(p.getAttack()).heal(p.getHeal()).defence(p.getDefence()).isBot(p.getIsBot()).hp(p.getHp())
                    .isEnter(p.getIsEnter()).enteredAt(p.getEnteredAt()).exitedAt(p.getExitedAt())
                    .build();
        }
    }

    public record PickDto(Long pickId, String playerId, String targetPlayerId, Integer round, String pickCode, Double pickValue) {
        public static PickDto of(MatchPick pick) {
            return new PickDto(
                    pick.getPickId(),
                    pick.getMatchPlayer() == null ? null : pick.getMatchPlayer().getPlayerId(),
                    pick.getTargetMatchPlayer() == null ? null : pick.getTargetMatchPlayer().getPlayerId(),
                    pick.getRound(),
                    pick.getPickCode() == null ? null : pick.getPickCode().name(),
                    pick.getPickValue());
        }
    }

    public static AdminMatchResponseDto of(Match match) {
        return AdminMatchResponseDto.builder()
                .matchId(match.getMatchId())
                .round(match.getRound())
                .maxRound(match.getMaxRound())
                .stateCode(match.getStateCode() == null ? null : match.getStateCode().name())
                .players(match.getMatchPlayers().stream().map(PlayerDto::of).toList())
                .picks(match.getMatchPicks().stream().map(PickDto::of).toList())
                .build();
    }
}
