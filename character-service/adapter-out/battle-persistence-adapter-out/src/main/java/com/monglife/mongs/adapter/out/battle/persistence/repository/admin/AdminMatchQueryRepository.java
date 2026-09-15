package com.monglife.mongs.adapter.out.battle.persistence.repository.admin;

import com.monglife.mongs.adapter.out.battle.persistence.entity.MatchEntity;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.domain.battle.enums.MatchStateCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.monglife.mongs.adapter.out.battle.persistence.entity.QMatchEntity.matchEntity;

/**
 * 관리자 매치 조회. 기존 {@code MatchRepository} 는 손대지 않고 별도 빈으로 둔다.
 */
@Repository
public class AdminMatchQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public AdminMatchQueryRepository(@Qualifier("battleJpaQueryFactory") JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<MatchEntity> findPage(AdminPageRequestVo pageRequest, MatchStateCode stateCode, Long accountId) {
        return jpaQueryFactory.selectFrom(matchEntity)
                .where(condition(stateCode, accountId, null))
                .orderBy(order(pageRequest))
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getSize())
                .fetch();
    }

    public Long count(MatchStateCode stateCode, Long accountId, LocalDateTime since) {
        return Optional.ofNullable(jpaQueryFactory.select(matchEntity.count())
                .from(matchEntity)
                .where(condition(stateCode, accountId, since))
                .fetchOne()).orElse(0L);
    }

    public Long countBotMatchesSince(LocalDateTime since) {
        BooleanBuilder builder = condition(null, null, since);
        builder.and(matchEntity.matchPlayers.any().isBot.isTrue());
        return Optional.ofNullable(jpaQueryFactory.select(matchEntity.count())
                .from(matchEntity)
                .where(builder)
                .fetchOne()).orElse(0L);
    }

    private BooleanBuilder condition(MatchStateCode stateCode, Long accountId, LocalDateTime since) {
        BooleanBuilder builder = new BooleanBuilder();
        if (stateCode != null) builder.and(matchEntity.stateCode.eq(stateCode));
        if (since != null) builder.and(matchEntity.createdAt.goe(since));
        if (accountId != null) builder.and(matchEntity.matchPlayers.any().accountId.eq(accountId));
        return builder;
    }

    private OrderSpecifier<?> order(AdminPageRequestVo pageRequest) {
        boolean desc = !Boolean.FALSE.equals(pageRequest.getSortDesc());
        return switch (pageRequest.getSortKey() == null ? "" : pageRequest.getSortKey()) {
            case "createdAt" -> desc ? matchEntity.createdAt.desc() : matchEntity.createdAt.asc();
            default -> desc ? matchEntity.matchId.desc() : matchEntity.matchId.asc();
        };
    }
}
