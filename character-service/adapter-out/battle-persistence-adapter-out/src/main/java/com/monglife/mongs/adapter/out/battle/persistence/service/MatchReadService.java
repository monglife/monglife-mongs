package com.monglife.mongs.adapter.out.battle.persistence.service;

import com.monglife.mongs.adapter.out.battle.persistence.entity.MatchEntity;
import com.monglife.mongs.adapter.out.battle.persistence.repository.MatchRepository;
import com.monglife.mongs.application.battle.port.out.MatchReadPort;
import com.monglife.mongs.domain.battle.model.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchReadService implements MatchReadPort {

    private final MatchRepository matchRepository;

    /**
     * 매치 조회
     * @param matchId 매치 ID
     * @return 매치 도메인 객체
     */
    @Override
    @Transactional
    public Optional<Match> getMatchPort(Long matchId) {
        return matchRepository.findByMatchId(matchId)
                .map(MatchEntity::toDomain)
                .or(Optional::empty);
    }

    /**
     * 입장 기한이 지난 ENTERING 매치 ID 목록
     * @param threshold 이 시각보다 오래된 매치
     * @param limit 한 번에 가져올 최대 건수
     * @return 매치 ID 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<Long> getExpiredEnteringMatchIdsPort(LocalDateTime threshold, int limit) {
        return matchRepository.findExpiredEnteringMatchIds(threshold, limit);
    }
}
