package com.monglife.mongs.adapter.out.member.persistence.repository.admin;

import com.monglife.mongs.adapter.out.member.persistence.entity.MemberEntity;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.monglife.mongs.adapter.out.member.persistence.entity.QMemberEntity.memberEntity;

@Repository
public class AdminMemberQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public AdminMemberQueryRepository(@Qualifier("memberJpaQueryFactory") JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<MemberEntity> findPage(AdminPageRequestVo pageRequest, Long accountId) {
        return jpaQueryFactory.selectFrom(memberEntity)
                .where(condition(accountId))
                .orderBy(order(pageRequest))
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getSize())
                .fetch();
    }

    public Long count(Long accountId) {
        return Optional.ofNullable(jpaQueryFactory.select(memberEntity.count())
                .from(memberEntity)
                .where(condition(accountId))
                .fetchOne()).orElse(0L);
    }

    public Long countJoinedSince(LocalDateTime since) {
        return Optional.ofNullable(jpaQueryFactory.select(memberEntity.count())
                .from(memberEntity)
                .where(memberEntity.createdAt.goe(since))
                .fetchOne()).orElse(0L);
    }

    public Long sumStarPoint() {
        Integer sum = jpaQueryFactory.select(memberEntity.starPoint.sum())
                .from(memberEntity)
                .fetchOne();
        return sum == null ? 0L : sum.longValue();
    }

    private BooleanBuilder condition(Long accountId) {
        BooleanBuilder builder = new BooleanBuilder();
        if (accountId != null) {
            builder.and(memberEntity.accountId.eq(accountId));
        }
        return builder;
    }

    private OrderSpecifier<?> order(AdminPageRequestVo pageRequest) {
        boolean desc = !Boolean.FALSE.equals(pageRequest.getSortDesc());
        return switch (pageRequest.getSortKey() == null ? "" : pageRequest.getSortKey()) {
            case "starPoint" -> desc ? memberEntity.starPoint.desc() : memberEntity.starPoint.asc();
            case "slotCount" -> desc ? memberEntity.slotCount.desc() : memberEntity.slotCount.asc();
            case "createdAt" -> desc ? memberEntity.createdAt.desc() : memberEntity.createdAt.asc();
            default -> desc ? memberEntity.accountId.desc() : memberEntity.accountId.asc();
        };
    }
}
