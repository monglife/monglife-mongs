package com.monglife.mongs.adapter.out.mong.persistence.service;

import com.monglife.core.vo.page.PageResult;
import com.monglife.mongs.adapter.out.mong.persistence.entity.*;
import com.monglife.mongs.adapter.out.mong.persistence.repository.*;
import com.monglife.mongs.domain.mong.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MongReadService implements
        com.monglife.mongs.application.mong.port.out.MongReadPort,
        com.monglife.mongs.application.battle.port.out.MongReadPort {

    private final MongStrokeHistoryRepository mongStrokeHistoryRepository;

    private final MongFeedHistoryRepository mongFeedHistoryRepository;

    private final FoodRepository foodRepository;

    private final SnackRepository snackRepository;

    private final MongTypeRepository mongTypeRepository;

    private final TrainingTypeRepository trainingTypeRepository;

    private final MongRepository mongRepository;

    private final RandomDrawRepository randomDrawRepository;

    private final InventoryRepository inventoryRepository;

    private final MongEvolutionHistoryRepository mongEvolutionHistoryRepository;

    /**
     * 몽 쓰다 듬기 대기 잔여 시간 조회
     * @param mongId 몽 ID
     * @return 쓰다 듬기 대기 잔여 시간 (초)
     */
    @Override
    @Transactional
    public Long getMongStrokeExpirationSecondsPort(Long mongId) {
        return mongStrokeHistoryRepository.findByMongId(mongId)
                .map(mongStrokeHistoryEntity -> mongStrokeHistoryEntity.getExpiration() - Duration.between(mongStrokeHistoryEntity.getStrokedAt(), LocalDateTime.now()).getSeconds())
                .orElse(0L);
    }

    /**
     * 몽 타입 목록 조회
     * @param level 몽 타입 레벨
     * @return 몽 타입 목록
     */
    @Override
    @Transactional
    public List<MongType> getMongTypesPort(Integer level) {
        return mongTypeRepository.findByLevel(level).stream()
                .map(MongTypeEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 다음 레벨 몽 타입 목록 조회
     * @param mongCode 현재 몽 타입 코드
     * @return 몽 타입 목록
     */
    @Override
    @Transactional
    public List<MongType> getNextLevelMongTypesPort(String mongCode) {
        return mongTypeRepository.findMongCode(mongCode).stream()
                .map(MongTypeEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 몽 진화 이력 목록 조회
     * @param accountId 계정 ID
     * @return 몽 진화 이력 목록
     */
    @Override
    public List<MongEvolutionHistory> getMongEvolutionHistoriesPort(Long accountId) {

        List<MongEvolutionHistoryEntity> histories = mongEvolutionHistoryRepository.findByAccountId(accountId);

        if (histories.isEmpty()) {
            return List.of();
        }

        // 이력 표는 코드만 들고 있다. 이름은 마스터에 있으므로 한 번에 읽어 붙인다 - 행마다 찾으면 N+1 이다.
        Set<String> mongCodes = histories.stream().map(MongEvolutionHistoryEntity::getMongCode).collect(Collectors.toSet());
        Map<String, String> nameByCode = mongTypeRepository.findByComnCodeIn(mongCodes).stream()
                .collect(Collectors.toMap(entity -> entity.getComn().getCode(), entity -> entity.getComn().getName()));

        return histories.stream()
                // 마스터에서 지워진 코드면 이름이 null 이다. 이력 자체는 남긴다.
                .map(entity -> entity.toDomain(nameByCode.get(entity.getMongCode())))
                .collect(Collectors.toList());
    }

    /**
     * 몽 조회
     * @param mongId 몽 ID
     * @return 몽 도메인 객체
     */
    @Override
    @Transactional
    public Optional<Mong> getMongPort(Long mongId) {
        return mongRepository.findByMongId(mongId)
                .map(MongEntity::toDomain)
                .or(Optional::empty);
    }

    /**
     * 몽 목록 조회
     * @param accountId 계정 ID
     * @return 몽 도메인 객체 목록
     */
    @Override
    @Transactional
    public List<Mong> getMongsPort(Long accountId) {
        return mongRepository.findAllByAccountId(accountId).stream()
                .map(MongEntity::toDomain)
                .toList();
    }

    /**
     * 음식 조회
     * @param foodCode 음식 타입 코드
     * @param mongId 몽 ID
     * @return 음식 도메인 객체
     */
    @Override
    @Transactional
    public Optional<Food> getFoodPort(String foodCode, Long mongId) {

        Optional<FoodEntity> foodEntityOptional = foodRepository.findByComnCode(foodCode);

        if (foodEntityOptional.isPresent()) {
            FoodEntity foodEntity = foodEntityOptional.get();
            boolean isCanBuy = mongFeedHistoryRepository.findByMongIdAndCode(mongId, foodCode).isEmpty();

            Food food = Food.builder()
                    .foodCode(foodEntity.getComn().getCode())
                    .foodName(foodEntity.getComn().getName())
                    .price(foodEntity.getPrice())
                    .isCanBuy(isCanBuy)
                    .weight(foodEntity.getWeight())
                    .strength(foodEntity.getStrength())
                    .satiety(foodEntity.getSatiety())
                    .healthy(foodEntity.getHealthy())
                    .fatigue(foodEntity.getFatigue())
                    .build();

            return Optional.of(food);
        }

        return Optional.empty();
    }

    /**
     * 음식 목록 조회
     * @param mongId 몽 ID
     * @return 음식 도메인 객체 목록
     */
    @Override
    @Transactional
    public List<Food> getFoodsPort(Long mongId) {

        List<String> invalidBuyFoodCodes = mongFeedHistoryRepository.findByMongId(mongId).stream()
                .map(MongFeedHistoryEntity::getCode)
                .toList();

        return foodRepository.findAll().stream()
                .map(foodEntity -> Food.builder()
                            .foodCode(foodEntity.getComn().getCode())
                            .foodName(foodEntity.getComn().getName())
                            .price(foodEntity.getPrice())
                            .isCanBuy(!invalidBuyFoodCodes.contains(foodEntity.getComn().getCode()))
                            .weight(foodEntity.getWeight())
                            .strength(foodEntity.getStrength())
                            .satiety(foodEntity.getSatiety())
                            .healthy(foodEntity.getHealthy())
                            .fatigue(foodEntity.getFatigue())
                            .build())
                .toList();
    }

    /**
     * 간식 조회
     * @param snackCode 간식 타입 코드
     * @param mongId 몽 ID
     * @return 간식 도메인 객체
     */
    @Override
    @Transactional
    public Optional<Snack> getSnackPort(String snackCode, Long mongId) {

        Optional<SnackEntity> snackEntityOptional = snackRepository.findByComnCode(snackCode);

        if (snackEntityOptional.isPresent()) {
            SnackEntity snackEntity = snackEntityOptional.get();
            boolean isCanBuy = mongFeedHistoryRepository.findByMongIdAndCode(mongId, snackCode).isEmpty();

            Snack snack = Snack.builder()
                    .snackCode(snackEntity.getComn().getCode())
                    .snackName(snackEntity.getComn().getName())
                    .price(snackEntity.getPrice())
                    .isCanBuy(isCanBuy)
                    .weight(snackEntity.getWeight())
                    .strength(snackEntity.getStrength())
                    .satiety(snackEntity.getSatiety())
                    .healthy(snackEntity.getHealthy())
                    .fatigue(snackEntity.getFatigue())
                    .build();

            return Optional.of(snack);
        }

        return Optional.empty();
    }

    /**
     * 간식 목록 조회
     * @param mongId 몽 ID
     * @return 간식 도메인 객체 목록
     */
    @Override
    @Transactional
    public List<Snack> getSnacksPort(Long mongId) {

        List<String> invalidBuySnackCodes = mongFeedHistoryRepository.findByMongId(mongId).stream()
                .map(MongFeedHistoryEntity::getCode)
                .toList();

        return snackRepository.findAll().stream()
                .map(snackEntity -> Snack.builder()
                        .snackCode(snackEntity.getComn().getCode())
                        .snackName(snackEntity.getComn().getName())
                        .price(snackEntity.getPrice())
                        .isCanBuy(!invalidBuySnackCodes.contains(snackEntity.getComn().getCode()))
                        .weight(snackEntity.getWeight())
                        .strength(snackEntity.getStrength())
                        .satiety(snackEntity.getSatiety())
                        .healthy(snackEntity.getHealthy())
                        .fatigue(snackEntity.getFatigue())
                        .build())
                .toList();
    }

    /**
     * 훈련 타입 목록 조회
     * @return 훈련 타입 도메인 객체 목록
     */
    @Override
    @Transactional
    public List<TrainingType> getTrainingTypesPort() {
        return trainingTypeRepository.findAll().stream()
                .map(TrainingTypeEntity::toDomain)
                .toList();
    }

    /**
     * 훈련 타입 조회
     * @param trainingCode 훈련 타입 코드
     * @return 훈련 타입 도메인 객체
     */
    @Override
    @Transactional
    public Optional<TrainingType> getTrainingTypePort(String trainingCode) {
        return trainingTypeRepository.findByComnCode(trainingCode)
                .map(TrainingTypeEntity::toDomain)
                .or(Optional::empty);
    }

    /**
     * 랜덤 뽑기 아이템 목록 조회
     * @return 랜덤 뽑기 아이템 도메인 객체 목록
     */
    @Override
    @Transactional
    public List<RandomDraw> getRandomDrawsPort(Long accountId) {
        return randomDrawRepository.findNotDrawByAccountId(accountId).stream()
                .map(RandomDrawEntity::toDomain)
                .toList();
    }

    /**
     * 인벤 아이템 목록 조회
     * @param mongId 몽 ID
     * @return 인벤 아이템 도메인 객체 목록
     */
    @Override
    @Transactional
    public PageResult<Inventory> getInventoriesPort(Long mongId, Integer page, Integer size) {

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "inventoryId"));

        Page<InventoryEntity> inventoriesPage = inventoryRepository.findByMongId(pageRequest, mongId);

        List<Inventory> inventories = inventoriesPage.getContent()
                .stream()
                .map(InventoryEntity::toDomain)
                .toList();

        return PageResult.<Inventory>builder()
                .page(page)
                .size(size)
                .totalPage(inventoriesPage.getTotalPages())
                .isLastPage(page == inventoriesPage.getTotalPages())
                .result(inventories)
                .build();
    }
}
