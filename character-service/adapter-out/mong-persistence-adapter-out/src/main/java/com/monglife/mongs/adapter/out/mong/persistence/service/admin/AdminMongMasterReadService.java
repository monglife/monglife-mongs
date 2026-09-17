package com.monglife.mongs.adapter.out.mong.persistence.service.admin;

import com.monglife.module.common.jpa.entity.ComnCodeEntity;
import com.monglife.module.common.jpa.entity.GroupCodeEntity;
import com.monglife.mongs.adapter.out.mong.persistence.entity.*;
import com.monglife.mongs.adapter.out.mong.persistence.repository.*;
import com.monglife.mongs.application.mong.port.exception.NotExistsMasterCodeException;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMasterCommand;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminFeedItemVo;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMongTypeVo;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongMasterReadPort;
import com.monglife.mongs.domain.mong.model.RandomDraw;
import com.monglife.mongs.domain.mong.model.TrainingType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMongMasterReadService implements AdminMongMasterReadPort {

    /** 종류별 공통 코드 그룹. monglife_group_code 의 시드와 같아야 한다 */
    private static final String GROUP_MONG = "CH";
    private static final String GROUP_FOOD = "FD";
    private static final String GROUP_SNACK = "SN";
    private static final String GROUP_TRAINING = "TR";

    private final MongTypeRepository mongTypeRepository;

    private final FoodRepository foodRepository;

    private final SnackRepository snackRepository;

    private final TrainingTypeRepository trainingTypeRepository;

    private final RandomDrawRepository randomDrawRepository;

    private final ComnCodeRepository comnCodeRepository;

    private final GroupCodeRepository groupCodeRepository;

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

    @Override
    @Transactional
    public Boolean isExistsComnCodePort(String code) {
        return comnCodeRepository.existsById(code);
    }

    @Override
    @Transactional
    public void createMongTypePort(AdminCreateMasterCommand command) {
        ComnCodeEntity comn = comnCode(command, GROUP_MONG);
        mongTypeRepository.save(MongTypeEntity.builder()
                .comn(comn)
                .level(command.getLevel())
                .evolutionScore(command.getEvolutionScore())
                .maxStatus(command.getMaxStatus())
                .groupType(command.getGroupType())
                .build());
    }

    @Override
    @Transactional
    public void createFoodPort(AdminCreateMasterCommand command) {
        ComnCodeEntity comn = comnCode(command, GROUP_FOOD);
        foodRepository.save(FoodEntity.builder()
                .comn(comn)
                .price(command.getPrice())
                .weight(command.getWeight())
                .strength(command.getStrength())
                .satiety(command.getSatiety())
                .healthy(command.getHealthy())
                .fatigue(command.getFatigue())
                .delaySeconds(command.getDelaySeconds())
                .build());
    }

    @Override
    @Transactional
    public void createSnackPort(AdminCreateMasterCommand command) {
        ComnCodeEntity comn = comnCode(command, GROUP_SNACK);
        snackRepository.save(SnackEntity.builder()
                .comn(comn)
                .price(command.getPrice())
                .weight(command.getWeight())
                .strength(command.getStrength())
                .satiety(command.getSatiety())
                .healthy(command.getHealthy())
                .fatigue(command.getFatigue())
                .delaySeconds(command.getDelaySeconds())
                .build());
    }

    @Override
    @Transactional
    public void createTrainingTypePort(AdminCreateMasterCommand command) {
        ComnCodeEntity comn = comnCode(command, GROUP_TRAINING);
        trainingTypeRepository.save(TrainingTypeEntity.builder()
                .comn(comn)
                .payPoint(command.getPayPoint())
                .score(command.getScore())
                .timeout(command.getTimeout())
                .exp(command.getExp())
                .strength(command.getStrength())
                .weight(command.getWeight())
                .satiety(command.getSatiety())
                .fatigue(command.getFatigue())
                .build());
    }

    /** 랜덤 뽑기는 이미 있는 코드를 뽑기 풀에 한 줄 더 얹는 것이다 */
    @Override
    @Transactional
    public void createRandomDrawPort(AdminCreateMasterCommand command) {
        ComnCodeEntity comn = comnCodeRepository.findById(command.getCode())
                .orElseThrow(NotExistsMasterCodeException::new);
        randomDrawRepository.save(RandomDrawEntity.builder()
                .comn(comn)
                .inventoryTypeCode(command.getInventoryTypeCode())
                .build());
    }

    /**
     * 공통 코드를 만들어 붙인다. 그룹 코드가 없으면 함께 만든다 —
     * 시드가 들어가지 않은 환경(빈 DB)에서도 등록이 되게 한다.
     */
    private ComnCodeEntity comnCode(AdminCreateMasterCommand command, String groupCode) {
        return comnCodeRepository.findById(command.getCode()).orElseGet(() -> {
            GroupCodeEntity group = groupCodeRepository.findById(groupCode)
                    .orElseGet(() -> groupCodeRepository.save(GroupCodeEntity.builder().code(groupCode).name(groupCode).build()));
            return comnCodeRepository.save(ComnCodeEntity.builder()
                    .code(command.getCode())
                    .name(command.getName())
                    .group(group)
                    .build());
        });
    }


    @Override
    @Transactional
    public Boolean isExistsMongTypePort(String code) {
        return mongTypeRepository.findByComnCode(code).isPresent();
    }

    @Override
    @Transactional
    public Boolean isExistsFoodPort(String code) {
        return foodRepository.findByComnCode(code).isPresent();
    }

    @Override
    @Transactional
    public Boolean isExistsSnackPort(String code) {
        return snackRepository.findByComnCode(code).isPresent();
    }

    @Override
    @Transactional
    public Boolean isExistsTrainingTypePort(String code) {
        return trainingTypeRepository.findByComnCode(code).isPresent();
    }

    @Override
    @Transactional
    public Boolean deleteMongTypePort(Long id) {
        // MongTypeRepository 의 ID 타입은 String(코드)이라 findById 를 쓸 수 없다
        return mongTypeRepository.findByMongTypeId(id)
                .map(entity -> {
                    mongTypeRepository.delete(entity);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public Boolean deleteFoodPort(Long id) {
        return delete(foodRepository, id);
    }

    @Override
    @Transactional
    public Boolean deleteSnackPort(Long id) {
        return delete(snackRepository, id);
    }

    @Override
    @Transactional
    public Boolean deleteTrainingTypePort(Long id) {
        return delete(trainingTypeRepository, id);
    }

    @Override
    @Transactional
    public Boolean deleteRandomDrawPort(Long id) {
        return delete(randomDrawRepository, id);
    }

    /** 없으면 false. 지우려던 행이 이미 없는 것과 실패를 호출 쪽이 구분할 수 있게 한다 */
    private <T> Boolean delete(JpaRepository<T, Long> repository, Long id) {
        return repository.findById(id)
                .map(entity -> {
                    repository.delete(entity);
                    return true;
                })
                .orElse(false);
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
