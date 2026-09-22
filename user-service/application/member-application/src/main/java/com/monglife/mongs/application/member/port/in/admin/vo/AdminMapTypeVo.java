package com.monglife.mongs.application.member.port.in.admin.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminMapTypeVo {

    private final Long mapTypeId;

    private final String mapCode;

    private final String mapName;

    private final String words;

    @Builder
    public AdminMapTypeVo(Long mapTypeId, String mapCode, String mapName, String words) {
        this.mapTypeId = mapTypeId;
        this.mapCode = mapCode;
        this.mapName = mapName;
        this.words = words;
    }
}
