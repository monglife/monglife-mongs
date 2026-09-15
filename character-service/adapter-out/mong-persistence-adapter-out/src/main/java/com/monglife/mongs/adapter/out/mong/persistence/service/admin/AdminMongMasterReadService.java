package com.monglife.mongs.adapter.out.mong.persistence.service.admin;

import com.monglife.mongs.adapter.out.mong.persistence.entity.FoodEntity;
import com.monglife.mongs.adapter.out.mong.persistence.entity.RandomDrawEntity;
import com.monglife.mongs.adapter.out.mong.persistence.entity.SnackEntity;
import com.monglife.mongs.adapter.out.mong.persistence.entity.TrainingTypeEntity;
import com.monglife.mongs.adapter.out.mong.persistence.repository.*;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminFeedItemVo;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMongTypeVo;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongMasterReadPort;
import com.monglife.mongs.domain.mong.model.RandomDraw;
import com.monglife.mongs.domain.mong.model.TrainingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMongMasterReadService implements AdminMongMasterReadPort {

    private final MongTypeRepository mongTypeRepository;

    private final FoodRepository foodRepository;

    private final SnackRepository snackRepository;

    private final TrainingTypeRepository trainingTypeRepository;

    private final RandomDrawRepository randomDrawRepository;

    @Override
    @Transactional
    public List<AdminMongTypeVo> getMongTypesPort() {
        return mongTypeRepository.findAll().stream()
                .map(entity -> AdminMongTypeVo.builder()
                        .mongTypeId(entity.getMongTypeId())
                        .mongCode(entity.getComn().getCode())
                        .mongName(entity.getComn().getName())
                        .level(entity.getLevel())
                        .evolutionScore(entity.getEvolutionScore())
                        .maxStatus(entity.getMaxStatus())
                        .groupType(entity.getGroupType())
                        .build())
                .sorted(Comparator.comparing(AdminMongTypeVo::getMongCode))
                .toList();
    }

    @Override
    @Transactional
    public List<AdminFeedItemVo> getFoodsPort() {
        return foodRepository.findAll().stream()
                .map(this::toVo)
                .sorted(Comparator.comparing(AdminFeedItemVo::getCode))
                .toList();
    }

    @Override
    @Transactional
    public List<AdminFeedItemVo> getSnacksPort() {
        return snackRepository.findAll().stream()
                .map(this::toVo)
                .sorted(Comparator.comparing(AdminFeedItemVo::getCode))
                .toList();
    }

    @Override
    @Transactional
    public List<TrainingType> getTrainingTypesPort() {
        return trainingTypeRepository.findAll().stream()
                .map(TrainingTypeEntity::toDomain)
                .sorted(Comparator.comparing(TrainingType::getTrainingCode))
                .toList();
    }

    @Override
    @Transactional
    public List<RandomDraw> getRandomDrawsPort() {
        return randomDrawRepository.findAll().stream()
                .map(RandomDrawEntity::toDomain)
                .sorted(Comparator.comparing(RandomDraw::getRandomDrawCode))
                .toList();
    }

    private AdminFeedItemVo toVo(FoodEntity entity) {
        return AdminFeedItemVo.builder()
                .id(entity.getFoodId())
                .code(entity.getComn().getCode())
                .name(entity.getComn().getName())
                .price(entity.getPrice())
                .weight(entity.getWeight())
                .strength(entity.getStrength())
                .satiety(entity.getSatiety())
                .healthy(entity.getHealthy())
                .fatigue(entity.getFatigue())
                .delaySeconds(entity.getDelaySeconds())
                .build();
    }

    private AdminFeedItemVo toVo(SnackEntity entity) {
        return AdminFeedItemVo.builder()
                .id(entity.getSnackId())
                .code(entity.getComn().getCode())
                .name(entity.getComn().getName())
                .price(entity.getPrice())
                .weight(entity.getWeight())
                .strength(entity.getStrength())
                .satiety(entity.getSatiety())
                .healthy(entity.getHealthy())
                .fatigue(entity.getFatigue())
                .delaySeconds(entity.getDelaySeconds())
                .build();
    }
}
