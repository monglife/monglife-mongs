package com.monglife.mongs.adapter.in.admin.user.web.dto.response;

import com.monglife.mongs.application.member.port.in.admin.vo.AdminMapTypeVo;

public record AdminMapTypeResponseDto(Long mapTypeId, String mapCode, String mapName, String words) {

    public static AdminMapTypeResponseDto of(AdminMapTypeVo vo) {
        return new AdminMapTypeResponseDto(vo.getMapTypeId(), vo.getMapCode(), vo.getMapName(), vo.getWords());
    }
}
