package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.monglife.mongs.domain.mong.model.RandomDraw;

public record AdminRandomDrawResponseDto(Long randomDrawId, String randomDrawCode, String randomDrawName, String inventoryTypeCode) {

    public static AdminRandomDrawResponseDto of(RandomDraw r) {
        return new AdminRandomDrawResponseDto(r.getRandomDrawId(), r.getRandomDrawCode(), r.getRandomDrawName(), r.getInventoryTypeCode() == null ? null : r.getInventoryTypeCode().name());
    }
}
