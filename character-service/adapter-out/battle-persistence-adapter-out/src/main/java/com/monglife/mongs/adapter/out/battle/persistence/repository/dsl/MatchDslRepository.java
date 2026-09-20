package com.monglife.mongs.adapter.out.battle.persistence.repository.dsl;

import com.monglife.mongs.adapter.out.battle.persistence.entity.MatchEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchDslRepository {

    Optional<MatchEntity> findByMatchIdWithLock(Long matchId);

    /** 입장 기한이 지난 ENTERING 매치 ID. 잠그지 않는다 - 갭 락이 새 매치 INSERT 를 막는다 */
    List<Long> findExpiredEnteringMatchIds(LocalDateTime threshold, int limit);
}
