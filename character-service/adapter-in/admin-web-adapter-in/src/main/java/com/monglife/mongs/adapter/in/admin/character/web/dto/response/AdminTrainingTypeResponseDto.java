package com.monglife.mongs.adapter.in.admin.character.web.dto.response;

import com.monglife.mongs.domain.mong.model.TrainingType;

public record AdminTrainingTypeResponseDto(Long trainingTypeId, String trainingCode, String trainingName, Integer payPoint, Integer score, Integer timeout, Double exp, Double strength, Double weight, Double satiety, Double fatigue) {

    public static AdminTrainingTypeResponseDto of(TrainingType t) {
        return new AdminTrainingTypeResponseDto(t.getTrainingTypeId(), t.getTrainingCode(), t.getTrainingName(), t.getPayPoint(), t.getScore(), t.getTimeout(), t.getExp(), t.getStrength(), t.getWeight(), t.getSatiety(), t.getFatigue());
    }
}
