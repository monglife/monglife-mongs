package com.monglife.mongs.adapter.out.battle.persistence.service.admin;

import com.monglife.mongs.adapter.out.battle.persistence.entity.MatchEntity;
import com.monglife.mongs.adapter.out.battle.persistence.entity.MatchPlayerEntity;
import com.monglife.mongs.adapter.out.battle.persistence.entity.QueuePlayerEntity;
import com.monglife.mongs.adapter.out.battle.persistence.repository.QueuePlayerRepository;
import com.monglife.mongs.adapter.out.battle.persistence.repository.admin.AdminMatchQueryRepository;
import com.monglife.mongs.application.battle.port.in.admin.vo.AdminMatchSummaryVo;
import com.monglife.mongs.application.battle.port.in.admin.vo.AdminQueuePlayerVo;
import com.monglife.mongs.application.battle.port.out.admin.AdminMatchReadPort;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.battle.enums.MatchStateCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminMatchReadService implements AdminMatchReadPort {

    private final QueuePlayerRepository queuePlayerRepository;

    private final AdminMatchQueryRepository adminMatchQueryRepository;

    @Override
    public List<AdminQueuePlayerVo> getQueuePlayersPort() {
        Set<QueuePlayerEntity> entities = queuePlayerRepository.findAll();
        if (entities == null) return List.of();
        return entities.stream()
                .map(entity -> AdminQueuePlayerVo.builder()
                        .mongId(entity.getMongId())
                        .deviceId(entity.getDeviceId())
                        .accountId(entity.getAccountId())
                        .createdAt(entity.getCreatedAt())
                        .build())
                .sorted(Comparator.comparing(AdminQueuePlayerVo::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    @Transactional
    public AdminPageVo<AdminMatchSummaryVo> getMatchesPort(AdminPageRequestVo pageRequest, MatchStateCode stateCode, Long accountId) {
        return AdminPageVo.<AdminMatchSummaryVo>builder()
                .page(pageRequest.getPage())
                .size(pageRequest.getSize())
                .total(adminMatchQueryRepository.count(stateCode, accountId, null))
                .items(adminMatchQueryRepository.findPage(pageRequest, stateCode, accountId).stream().map(this::toVo).toList())
                .build();
    }

    @Override
    @Transactional
    public Long countMatchesPort(MatchStateCode stateCode, LocalDateTime since) {
        return adminMatchQueryRepository.count(stateCode, null, since);
    }

    @Override
    @Transactional
    public Long countBotMatchesSincePort(LocalDateTime since) {
        return adminMatchQueryRepository.countBotMatchesSince(since);
    }

    private AdminMatchSummaryVo toVo(MatchEntity entity) {
        List<MatchPlayerEntity> players = entity.getMatchPlayers();
        return AdminMatchSummaryVo.builder()
                .matchId(entity.getMatchId())
                .round(entity.getRound())
                .maxRound(entity.getMaxRound())
                .stateCode(entity.getStateCode())
                .playerCount(players.size())
                .botCount((int) players.stream().filter(player -> Boolean.TRUE.equals(player.getIsBot())).count())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
