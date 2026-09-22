package com.monglife.mongs.adapter.in.admin.user.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminOrderDetailVo;
import com.monglife.mongs.domain.member.model.InAppOrder;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminOrderDetailResponseDto(
        AdminOrderResponseDto order,
        Integer starPoint,
        InAppOrderDto inAppOrder
) {
    /** 구글 플레이 조회 결과. 조회 실패 시 null */
    @Builder
    public record InAppOrderDto(String purchaseType, String orderType, Boolean isPayed, Boolean isConsumed, @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul") LocalDateTime purchasedAt) {
        public static InAppOrderDto of(InAppOrder inAppOrder) {
            if (inAppOrder == null) return null;
            return InAppOrderDto.builder()
                    .purchaseType(inAppOrder.getOrderPurchaseTypeCode() == null ? null : inAppOrder.getOrderPurchaseTypeCode().name())
                    .orderType(inAppOrder.getOrderTypeCode() == null ? null : inAppOrder.getOrderTypeCode().name())
                    .isPayed(inAppOrder.isPayed())
                    .isConsumed(inAppOrder.isConsumed())
                    .purchasedAt(inAppOrder.getPurchasedAt())
                    .build();
        }
    }

    public static AdminOrderDetailResponseDto of(AdminOrderDetailVo vo) {
        return AdminOrderDetailResponseDto.builder()
                .order(AdminOrderResponseDto.of(vo.getOrder()))
                .starPoint(vo.getStarPoint())
                .inAppOrder(InAppOrderDto.of(vo.getInAppOrder()))
                .build();
    }
}
