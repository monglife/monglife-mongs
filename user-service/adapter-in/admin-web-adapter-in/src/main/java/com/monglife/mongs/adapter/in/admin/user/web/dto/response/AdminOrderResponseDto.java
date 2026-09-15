package com.monglife.mongs.adapter.in.admin.user.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminOrderVo;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminOrderResponseDto(
        Long orderId,
        Long accountId,
        String productId,
        String productName,
        Double price,
        String socialOrderId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
        LocalDateTime createdAt
) {
    public static AdminOrderResponseDto of(AdminOrderVo vo) {
        return AdminOrderResponseDto.builder()
                .orderId(vo.getOrderId())
                .accountId(vo.getAccountId())
                .productId(vo.getProductId())
                .productName(vo.getProductName())
                .price(vo.getPrice())
                .socialOrderId(vo.getSocialOrderId())
                .createdAt(vo.getCreatedAt())
                .build();
    }
}
