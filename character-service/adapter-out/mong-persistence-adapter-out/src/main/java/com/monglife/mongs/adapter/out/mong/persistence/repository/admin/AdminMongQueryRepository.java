package com.monglife.mongs.adapter.out.mong.persistence.repository.admin;

import com.monglife.mongs.adapter.out.mong.persistence.entity.MongEntity;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.monglife.mongs.adapter.out.mong.persistence.entity.QMongEntity.mongEntity;

/**
 * 관리자 몽 조회. 기존 {@code MongRepository} 는 손대지 않고 별도 빈으로 둔다.
 */
@Repository
public class AdminMongQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public AdminMongQueryRepository(@Qualifier("mongJpaQueryFactory") JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<MongEntity> findPage(AdminPageRequestVo pageRequest, Long accountId, MongStateCode stateCode, MongStatusCode statusCode, String query) {
        return jpaQueryFactory.selectFrom(mongEntity)
                .leftJoin(mongEntity.mongType).fetchJoin()
                .where(condition(accountId, stateCode, statusCode, query))
                .orderBy(order(pageRequest))
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getSize())
                .fetch();
    }

    public Long count(Long accountId, MongStateCode stateCode, MongStatusCode statusCode, String query) {
        return Optional.ofNullable(jpaQueryFactory.select(mongEntity.count())
                .from(mongEntity)
                .where(condition(accountId, stateCode, statusCode, query))
                .fetchOne()).orElse(0L);
    }

    public Long countCreatedSince(LocalDateTime since) {
        return Optional.ofNullable(jpaQueryFactory.select(mongEntity.count())
                .from(mongEntity)
                .where(mongEntity.createdAt.goe(since))
                .fetchOne()).orElse(0L);
    }

    public Map<MongStateCode, Long> countByState() {
        Map<MongStateCode, Long> result = new EnumMap<>(MongStateCode.class);
        for (MongStateCode code : MongStateCode.values()) result.put(code, 0L);
        List<Tuple> tuples = jpaQueryFactory.select(mongEntity.stateCode, mongEntity.count())
                .from(mongEntity)
                .groupBy(mongEntity.stateCode)
                .fetch();
        for (Tuple tuple : tuples) {
            MongStateCode code = tuple.get(mongEntity.stateCode);
            Long count = tuple.get(mongEntity.count());
            if (code != null) result.put(code, count == null ? 0L : count);
        }
        return result;
    }

    public Map<MongStatusCode, Long> countByStatus() {
        Map<MongStatusCode, Long> result = new EnumMap<>(MongStatusCode.class);
        for (MongStatusCode code : MongStatusCode.values()) result.put(code, 0L);
        List<Tuple> tuples = jpaQueryFactory.select(mongEntity.statusCode, mongEntity.count())
                .from(mongEntity)
                .groupBy(mongEntity.statusCode)
                .fetch();
        for (Tuple tuple : tuples) {
            MongStatusCode code = tuple.get(mongEntity.statusCode);
            Long count = tuple.get(mongEntity.count());
            if (code != null) result.put(code, count == null ? 0L : count);
        }
        return result;
    }

    private BooleanBuilder condition(Long accountId, MongStateCode stateCode, MongStatusCode statusCode, String query) {
        BooleanBuilder builder = new BooleanBuilder();
        if (accountId != null) builder.and(mongEntity.accountId.eq(accountId));
        if (stateCode != null) builder.and(mongEntity.stateCode.eq(stateCode));
        if (statusCode != null) builder.and(mongEntity.statusCode.eq(statusCode));
        if (query != null && !query.isBlank()) builder.and(mongEntity.name.containsIgnoreCase(query));
        return builder;
    }

    private OrderSpecifier<?> order(AdminPageRequestVo pageRequest) {
        boolean desc = !Boolean.FALSE.equals(pageRequest.getSortDesc());
        return switch (pageRequest.getSortKey() == null ? "" : pageRequest.getSortKey()) {
            case "createdAt" -> desc ? mongEntity.createdAt.desc() : mongEntity.createdAt.asc();
            case "exp" -> desc ? mongEntity.exp.desc() : mongEntity.exp.asc();
            case "payPoint" -> desc ? mongEntity.payPoint.desc() : mongEntity.payPoint.asc();
            case "accountId" -> desc ? mongEntity.accountId.desc() : mongEntity.accountId.asc();
            default -> desc ? mongEntity.mongId.desc() : mongEntity.mongId.asc();
        };
    }
}
