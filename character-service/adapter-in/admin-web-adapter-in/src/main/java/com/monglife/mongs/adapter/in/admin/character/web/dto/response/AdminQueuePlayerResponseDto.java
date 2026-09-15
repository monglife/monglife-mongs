package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monglife.mongs.application.battle.port.in.admin.vo.AdminQueuePlayerVo;

import java.time.LocalDateTime;

public record AdminQueuePlayerResponseDto(Long mongId, String deviceId, Long accountId, @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul") LocalDateTime createdAt) {

    public static AdminQueuePlayerResponseDto of(AdminQueuePlayerVo vo) {
        return new AdminQueuePlayerResponseDto(vo.getMongId(), vo.getDeviceId(), vo.getAccountId(), vo.getCreatedAt());
    }
}
