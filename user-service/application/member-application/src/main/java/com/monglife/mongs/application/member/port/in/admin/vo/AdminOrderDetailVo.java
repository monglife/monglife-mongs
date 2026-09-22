package com.monglife.mongs.application.member.port.in.admin.vo;

import com.monglife.mongs.domain.member.model.InAppOrder;
import lombok.Builder;
import lombok.Getter;

/**
 * 관리자용 주문 상세. 구글 플레이 조회가 실패하면 {@code inAppOrder} 는 null 이다.
 */
@Getter
public class AdminOrderDetailVo {

    private final AdminOrderVo order;

    private final Integer starPoint;

    private final InAppOrder inAppOrder;

    @Builder
    public AdminOrderDetailVo(AdminOrderVo order, Integer starPoint, InAppOrder inAppOrder) {
        this.order = order;
        this.starPoint = starPoint;
        this.inAppOrder = inAppOrder;
    }
}
