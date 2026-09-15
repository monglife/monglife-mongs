package com.monglife.mongs.adapter.in.admin.user.web.dto.response;

import com.monglife.mongs.domain.member.model.CollectionMap;

public record AdminCollectionMapResponseDto(String mapCode, String mapName, Boolean isIncluded) {

    public static AdminCollectionMapResponseDto of(CollectionMap collectionMap) {
        return new AdminCollectionMapResponseDto(collectionMap.getMapCode(), collectionMap.getMapName(), collectionMap.getIsIncluded());
    }
}
