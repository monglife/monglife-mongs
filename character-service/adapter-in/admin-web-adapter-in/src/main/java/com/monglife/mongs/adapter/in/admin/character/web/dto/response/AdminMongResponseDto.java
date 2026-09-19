package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monglife.mongs.domain.mong.model.Mong;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
public record AdminMongResponseDto(
        Long mongId,
        Long accountId,
        String name,
        String mongCode,
        String mongName,
        String stateCode,
        String statusCode,
        Integer level,
        Double maxStatus,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
        LocalTime sleepAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
        LocalTime wakeupAt,
        Boolean isSleep,
        Integer payPoint,
        Double weight,
        Double strength,
        Double satiety,
        Double healthy,
        Double fatigue,
        Double exp,
        Double evolutionReward,
        Double evolutionPenalty,
        Integer strokeCount,
        Integer trainingCount,
        Integer poopCount,
        Integer randomDrawTicketCount,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
        LocalDateTime updatedAt
) {
    public static AdminMongResponseDto of(Mong mong) {
        return AdminMongResponseDto.builder()
                .mongId(mong.getMongId())
                .accountId(mong.getAccountId())
                .name(mong.getName())
                .mongCode(mong.getMongCode())
                .mongName(mong.getMongName())
                .stateCode(mong.getStateCode() == null ? null : mong.getStateCode().name())
                .statusCode(mong.getStatusCode() == null ? null : mong.getStatusCode().name())
                .level(mong.getLevel())
                .maxStatus(mong.getMaxStatus())
                .sleepAt(mong.getSleepAt())
                .wakeupAt(mong.getWakeupAt())
                .isSleep(mong.getIsSleep())
                .payPoint(mong.getPayPoint())
                .weight(mong.getWeight())
                .strength(mong.getStrength())
                .satiety(mong.getSatiety())
                .healthy(mong.getHealthy())
                .fatigue(mong.getFatigue())
                .exp(mong.getExp())
                .evolutionReward(mong.getEvolutionReward())
                .evolutionPenalty(mong.getEvolutionPenalty())
                .strokeCount(mong.getStrokeCount())
                .trainingCount(mong.getTrainingCount())
                .poopCount(mong.getPoopCount())
                .randomDrawTicketCount(mong.getRandomDrawTicketCount())
                .createdAt(mong.getCreatedAt())
                .updatedAt(mong.getUpdatedAt())
                .build();
    }
}
