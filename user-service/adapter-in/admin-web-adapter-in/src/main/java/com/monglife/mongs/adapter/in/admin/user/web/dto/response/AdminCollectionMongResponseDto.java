package com.monglife.mongs.adapter.in.admin.user.web.dto.response;

import com.monglife.mongs.domain.member.model.CollectionMong;

public record AdminCollectionMongResponseDto(String mongCode, String mongName, Boolean isIncluded) {

    public static AdminCollectionMongResponseDto of(CollectionMong collectionMong) {
        return new AdminCollectionMongResponseDto(collectionMong.getMongCode(), collectionMong.getMongName(), collectionMong.getIsIncluded());
    }
}
