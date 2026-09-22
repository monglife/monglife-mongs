package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMongTypeVo;

public record AdminMongTypeResponseDto(Long mongTypeId, String mongCode, String mongName, Integer level, Double evolutionScore, Double maxStatus, String groupType) {

    public static AdminMongTypeResponseDto of(AdminMongTypeVo vo) {
        return new AdminMongTypeResponseDto(vo.getMongTypeId(), vo.getMongCode(), vo.getMongName(), vo.getLevel(), vo.getEvolutionScore(), vo.getMaxStatus(), vo.getGroupType());
    }
}
