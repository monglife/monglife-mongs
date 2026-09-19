package com.monglife.mongs.application.member.port.in.admin.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 관리자용 주문 조회 결과. 구매 토큰은 상세에서만 내려주므로 여기엔 담지 않는다.
 */
@Getter
public class AdminOrderVo {

    private final Long orderId;

    private final Long accountId;

    private final String productId;

    private final String productName;

    private final Double price;

    private final String socialOrderId;

    private final LocalDateTime createdAt;

    @Builder
    public AdminOrderVo(Long orderId, Long accountId, String productId, String productName, Double price, String socialOrderId, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.socialOrderId = socialOrderId;
        this.createdAt = createdAt;
    }
}
