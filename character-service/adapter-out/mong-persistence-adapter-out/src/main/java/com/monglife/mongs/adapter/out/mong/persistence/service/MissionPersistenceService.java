package com.monglife.mongs.adapter.out.mong.persistence.service;

import com.monglife.mongs.adapter.out.mong.persistence.entity.AccountMissionDetailEntity;
import com.monglife.mongs.adapter.out.mong.persistence.entity.AccountMissionEntity;
import com.monglife.mongs.adapter.out.mong.persistence.repository.AccountMissionDetailRepository;
import com.monglife.mongs.adapter.out.mong.persistence.repository.AccountMissionRepository;
import com.monglife.mongs.application.mong.port.out.MissionPersistencePort;
import com.monglife.mongs.application.mong.port.out.vo.CreateAccountMissionVo;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.model.AccountMission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MissionPersistenceService implements MissionPersistencePort {

    private final AccountMissionRepository accountMissionRepository;

    private final AccountMissionDetailRepository accountMissionDetailRepository;

    /**
     * 사용자 미션 적재
     */
    @Override
    @Transactional
    public int createAccountMissionsPort(List<CreateAccountMissionVo> createAccountMissionVos) {

        int inserted = 0;

        for (CreateAccountMissionVo createAccountMissionVo : createAccountMissionVos) {
            inserted += accountMissionRepository.insertIgnore(
                    createAccountMissionVo.getAccountId(),
                    createAccountMissionVo.getMissionId(),
                    createAccountMissionVo.getCycleCode().name(),
                    createAccountMissionVo.getCycleKey());
        }

        return inserted;
    }

    /**
     * 진행도 갱신 대상 사용자 미션 조회 (행 잠금)
     */
    @Override
    @Transactional
    public List<AccountMission> getAccountMissionsForUpdatePort(Long accountId, Collection<String> cycleKeys, Collection<MissionActionCode> actionCodes) {

        List<AccountMissionEntity> accountMissionEntities =
                accountMissionRepository.findForUpdate(accountId, cycleKeys, actionCodes);

        return MissionReadService.toDomains(accountMissionEntities, accountMissionDetailRepository);
    }

    /**
     * 사용자 미션 조회 (행 잠금)
     */
    @Override
    @Transactional
    public Optional<AccountMission> getAccountMissionForUpdatePort(Long accountMissionId) {
        return accountMissionRepository.findByIdWithLock(accountMissionId)
                .map(entity -> MissionReadService
                        .toDomains(List.of(entity), accountMissionDetailRepository)
                        .get(0));
    }

    /**
     * 사용자 미션 상태 동기화
     */
    @Override
    @Transactional
    public void saveAccountMissionPort(AccountMission accountMission) {

        accountMissionRepository.findById(accountMission.getAccountMissionId())
                .ifPresent(entity -> entity.update(accountMission));

        // DISTINCT 미션에서 이번에 새로 집계된 대상만 적재한다.
        // 호출 경로가 이미 해당 행을 PESSIMISTIC_WRITE 로 잡고 있어 같은 코드가 두 번 들어오지 않는다.
        if (accountMission.getAddedDetailCode() != null) {
            accountMissionDetailRepository.save(AccountMissionDetailEntity.builder()
                    .accountMissionId(accountMission.getAccountMissionId())
                    .detailCode(accountMission.getAddedDetailCode())
                    .build());
        }
    }
}
