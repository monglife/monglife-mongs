package com.monglife.mongs.adapter.out.battle.persistence.repository.dsl;

import com.monglife.mongs.adapter.out.battle.persistence.entity.MatchEntity;
import com.monglife.mongs.domain.battle.enums.MatchStateCode;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.monglife.mongs.adapter.out.battle.persistence.entity.QMatchEntity.matchEntity;

@Repository
public class MatchDslRepositoryImpl implements MatchDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public MatchDslRepositoryImpl(@Qualifier("battleJpaQueryFactory") JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Optional<MatchEntity> findByMatchIdWithLock(Long matchId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(matchEntity)
                .where(matchEntity.matchId.eq(matchId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }

    @Override
    public List<Long> findExpiredEnteringMatchIds(LocalDateTime threshold, int limit) {
        return jpaQueryFactory.select(matchEntity.matchId)
                .from(matchEntity)
                .where(matchEntity.stateCode.eq(MatchStateCode.ENTERING)
                        .and(matchEntity.createdAt.loe(threshold)))
                .orderBy(matchEntity.matchId.asc())
                .limit(limit)
                .fetch();
    }
}
