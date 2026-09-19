package com.monglife.mongs.adapter.in.admin.user.web.dto.response;

import com.monglife.mongs.application.member.port.in.admin.vo.AdminExchangeStarPointProductVo;

public record AdminExchangeStarPointProductResponseDto(String productId, String productName, Integer starPoint) {

    public static AdminExchangeStarPointProductResponseDto of(AdminExchangeStarPointProductVo vo) {
        return new AdminExchangeStarPointProductResponseDto(vo.getProductId(), vo.getProductName(), vo.getStarPoint());
    }
}
