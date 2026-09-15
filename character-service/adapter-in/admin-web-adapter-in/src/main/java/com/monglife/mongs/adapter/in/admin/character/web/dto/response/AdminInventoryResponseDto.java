package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.monglife.mongs.domain.mong.model.Inventory;

public record AdminInventoryResponseDto(Long inventoryId, Long mongId, String inventoryCode, String inventoryName, String inventoryTypeCode) {

    public static AdminInventoryResponseDto of(Inventory inventory) {
        return new AdminInventoryResponseDto(
                inventory.getInventoryId(), inventory.getMongId(), inventory.getInventoryCode(), inventory.getInventoryName(),
                inventory.getInventoryTypeCode() == null ? null : inventory.getInventoryTypeCode().name());
    }
}
