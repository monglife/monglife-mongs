package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.monglife.mongs.application.mong.port.in.admin.vo.AdminFeedItemVo;

public record AdminFeedItemResponseDto(Long id, String code, String name, Integer price, Double weight, Double strength, Double satiety, Double healthy, Double fatigue, Integer delaySeconds) {

    public static AdminFeedItemResponseDto of(AdminFeedItemVo vo) {
        return new AdminFeedItemResponseDto(vo.getId(), vo.getCode(), vo.getName(), vo.getPrice(), vo.getWeight(), vo.getStrength(), vo.getSatiety(), vo.getHealthy(), vo.getFatigue(), vo.getDelaySeconds());
    }
}
