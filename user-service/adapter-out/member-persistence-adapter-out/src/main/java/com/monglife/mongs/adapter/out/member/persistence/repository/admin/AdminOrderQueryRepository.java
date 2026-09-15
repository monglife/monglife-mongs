package com.monglife.mongs.adapter.out.member.persistence.repository.admin;

import com.monglife.mongs.adapter.out.member.persistence.entity.OrderEntity;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.monglife.mongs.adapter.out.member.persistence.entity.QOrderEntity.orderEntity;

@Repository
public class AdminOrderQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public AdminOrderQueryRepository(@Qualifier("memberJpaQueryFactory") JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<OrderEntity> findPage(AdminPageRequestVo pageRequest, Long accountId, String productId) {
        return jpaQueryFactory.selectFrom(orderEntity)
                .leftJoin(orderEntity.productType).fetchJoin()
                .where(condition(accountId, productId))
                .orderBy(order(pageRequest))
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getSize())
                .fetch();
    }

    public Long count(Long accountId, String productId) {
        return Optional.ofNullable(jpaQueryFactory.select(orderEntity.count())
                .from(orderEntity)
                .where(condition(accountId, productId))
                .fetchOne()).orElse(0L);
    }

    public Optional<OrderEntity> findByOrderId(Long orderId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(orderEntity)
                .leftJoin(orderEntity.productType).fetchJoin()
                .where(orderEntity.orderId.eq(orderId))
                .fetchOne());
    }

    public Long countSince(LocalDateTime since) {
        return Optional.ofNullable(jpaQueryFactory.select(orderEntity.count())
                .from(orderEntity)
                .where(since == null ? null : orderEntity.createdAt.goe(since))
                .fetchOne()).orElse(0L);
    }

    public Double sumPriceSince(LocalDateTime since) {
        return Optional.ofNullable(jpaQueryFactory.select(orderEntity.price.sum())
                .from(orderEntity)
                .where(since == null ? null : orderEntity.createdAt.goe(since))
                .fetchOne()).orElse(0D);
    }

    private BooleanBuilder condition(Long accountId, String productId) {
        BooleanBuilder builder = new BooleanBuilder();
        if (accountId != null) {
            builder.and(orderEntity.accountId.eq(accountId));
        }
        if (productId != null && !productId.isBlank()) {
            builder.and(orderEntity.productType.code.eq(productId));
        }
        return builder;
    }

    private OrderSpecifier<?> order(AdminPageRequestVo pageRequest) {
        boolean desc = !Boolean.FALSE.equals(pageRequest.getSortDesc());
        return switch (pageRequest.getSortKey() == null ? "" : pageRequest.getSortKey()) {
            case "createdAt" -> desc ? orderEntity.createdAt.desc() : orderEntity.createdAt.asc();
            case "price" -> desc ? orderEntity.price.desc() : orderEntity.price.asc();
            default -> desc ? orderEntity.orderId.desc() : orderEntity.orderId.asc();
        };
    }
}
