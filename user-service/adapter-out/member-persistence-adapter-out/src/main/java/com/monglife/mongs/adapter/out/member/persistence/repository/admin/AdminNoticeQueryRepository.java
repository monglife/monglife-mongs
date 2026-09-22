package com.monglife.mongs.adapter.out.member.persistence.repository.admin;

import com.monglife.mongs.adapter.out.member.persistence.entity.NoticeEntity;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.monglife.mongs.adapter.out.member.persistence.entity.QNoticeEntity.noticeEntity;

/**
 * 관리자 공지 사항 조회. 기존 {@code NoticeRepository} 는 손대지 않고 별도 빈으로 둔다.
 */
@Repository
public class AdminNoticeQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public AdminNoticeQueryRepository(@Qualifier("memberJpaQueryFactory") JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<NoticeEntity> findPage(AdminPageRequestVo pageRequest, String query) {
        return jpaQueryFactory.selectFrom(noticeEntity)
                .where(condition(query))
                .orderBy(order(pageRequest))
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getSize())
                .fetch();
    }

    public Long count(String query) {
        return Optional.ofNullable(jpaQueryFactory.select(noticeEntity.count())
                .from(noticeEntity)
                .where(condition(query))
                .fetchOne()).orElse(0L);
    }

    private BooleanBuilder condition(String query) {
        BooleanBuilder builder = new BooleanBuilder();
        if (query != null && !query.isBlank()) {
            builder.and(noticeEntity.title.containsIgnoreCase(query)
                    .or(noticeEntity.content.containsIgnoreCase(query)));
        }
        return builder;
    }

    private OrderSpecifier<?> order(AdminPageRequestVo pageRequest) {
        boolean desc = !Boolean.FALSE.equals(pageRequest.getSortDesc());
        return switch (pageRequest.getSortKey() == null ? "" : pageRequest.getSortKey()) {
            case "createdAt" -> desc ? noticeEntity.createdAt.desc() : noticeEntity.createdAt.asc();
            default -> desc ? noticeEntity.noticeId.desc() : noticeEntity.noticeId.asc();
        };
    }
}
