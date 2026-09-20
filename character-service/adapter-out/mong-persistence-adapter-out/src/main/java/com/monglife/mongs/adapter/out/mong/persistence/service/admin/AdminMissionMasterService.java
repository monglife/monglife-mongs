package com.monglife.mongs.adapter.out.mong.persistence.service.admin;

import com.monglife.mongs.adapter.out.mong.persistence.entity.MissionEntity;
import com.monglife.mongs.adapter.out.mong.persistence.entity.MissionRewardEntity;
import com.monglife.mongs.adapter.out.mong.persistence.repository.AccountMissionRepository;
import com.monglife.mongs.adapter.out.mong.persistence.repository.MissionRepository;
import com.monglife.mongs.adapter.out.mong.persistence.repository.MissionRewardRepository;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMissionCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMissionCommand;
import com.monglife.mongs.application.mong.port.out.admin.AdminMissionMasterPort;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.model.Mission;
import com.monglife.mongs.domain.mission.model.MissionReward;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 미션 마스터 등록·노출 전환·삭제. 기존 마스터 조회 서비스와 달리 공통 코드를 만들지 않는다 —
 * 미션 코드는 다른 표에서 FK 로 참조되지 않기 때문이다.
 */
@Service
@RequiredArgsConstructor
public class AdminMissionMasterService implements AdminMissionMasterPort {

    private final MissionRepository missionRepository;

    private final MissionRewardRepository missionRewardRepository;

    private final AccountMissionRepository accountMissionRepository;

    @Override
    @Transactional
    public Boolean isExistsMissionCodePort(String missionCode) {
        return missionRepository.existsByMissionCode(missionCode);
    }

    @Override
    @Transactional
    public Boolean isExistsGoalInOtherCyclePort(MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, MissionCycleCode cycleCode) {
        return missionRepository.existsByActionCodeAndGoalTypeCodeAndCycleCodeNot(actionCode, goalTypeCode, cycleCode);
    }

    @Override
    @Transactional
    public Mission createMissionPort(AdminCreateMissionCommand command) {

        MissionEntity missionEntity = missionRepository.save(MissionEntity.builder()
                .missionCode(command.getMissionCode())
                .cycleCode(command.getCycleCode())
                .actionCode(command.getActionCode())
                .goalTypeCode(command.getGoalTypeCode())
                .title(command.getTitle())
                .description(command.getDescription())
                .goalCount(command.getGoalCount())
                .isActive(command.getIsActive() == null || command.getIsActive())
                .sortOrder(command.getSortOrder() == null ? 0 : command.getSortOrder())
                .rotationGroup(command.getRotationGroup())
                .build());

        for (MissionReward reward : command.getRewards()) {
            missionEntity.addReward(missionRewardRepository.save(MissionRewardEntity.builder()
                    .mission(missionEntity)
                    .rewardTypeCode(reward.getRewardTypeCode())
                    .rewardCode(reward.getRewardCode())
                    .inventoryTypeCode(reward.getInventoryTypeCode())
                    .amount(reward.getAmount())
                    .build()));
        }

        return missionEntity.toDomain();
    }

    @Override
    @Transactional
    public Optional<Mission> getMissionPort(Long missionId) {
        return missionRepository.findById(missionId).map(MissionEntity::toDomain);
    }

    @Override
    @Transactional
    public Boolean isExistsGoalPort(MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, Integer goalCount, Long excludeMissionId) {
        return missionRepository.existsByActionCodeAndGoalTypeCodeAndGoalCountAndMissionIdNot(actionCode, goalTypeCode, goalCount, excludeMissionId);
    }

    @Override
    @Transactional
    public Boolean isExistsGoalPort(MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, Integer goalCount) {
        return missionRepository.existsByActionCodeAndGoalTypeCodeAndGoalCount(actionCode, goalTypeCode, goalCount);
    }

    /**
     * 수정. 리워드는 부분 갱신이 아니라 통째 교체다.
     *
     * <p>지우고 다시 넣기 전에 {@code flush} 한다. 같은 트랜잭션에서 delete 가 INSERT 뒤로 밀리면
     * 방금 넣은 행까지 지워진다 - Hibernate 의 쓰기 순서는 SQL 작성 순서가 아니라 액션 큐 순서다.
     */
    @Override
    @Transactional
    public Optional<Mission> updateMissionPort(AdminUpdateMissionCommand command) {

        return missionRepository.findById(command.getMissionId())
                .map(missionEntity -> {
                    missionEntity.update(
                            command.getTitle(),
                            command.getDescription(),
                            command.getGoalCount(),
                            command.getIsActive() == null || command.getIsActive(),
                            command.getSortOrder() == null ? 0 : command.getSortOrder(),
                            command.getRotationGroup());

                    missionRewardRepository.deleteByMissionMissionId(command.getMissionId());
                    missionRewardRepository.flush();
                    missionEntity.clearRewards();

                    for (MissionReward reward : command.getRewards()) {
                        missionEntity.addReward(missionRewardRepository.save(MissionRewardEntity.builder()
                                .mission(missionEntity)
                                .rewardTypeCode(reward.getRewardTypeCode())
                                .rewardCode(reward.getRewardCode())
                                .inventoryTypeCode(reward.getInventoryTypeCode())
                                .amount(reward.getAmount())
                                .build()));
                    }

                    return missionEntity.toDomain();
                });
    }

    @Override
    @Transactional
    public Optional<Mission> updateMissionActivePort(Long missionId, Boolean isActive) {

        return missionRepository.findById(missionId)
                .map(missionEntity -> {
                    missionEntity.updateActive(isActive);
                    return missionEntity.toDomain();
                });
    }

    @Override
    @Transactional
    public Boolean deleteMissionPort(Long missionId) {

        return missionRepository.findById(missionId)
                .map(missionEntity -> {
                    missionRewardRepository.deleteByMissionMissionId(missionId);
                    missionRepository.delete(missionEntity);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public Boolean isExistsAccountMissionPort(Long missionId) {
        return accountMissionRepository.existsByMissionMissionId(missionId);
    }
}
