package com.monglife.mongs.adapter.out.mong.persistence.service;

import com.monglife.mongs.adapter.out.mong.persistence.entity.AccountMissionDetailEntity;
import com.monglife.mongs.adapter.out.mong.persistence.entity.AccountMissionEntity;
import com.monglife.mongs.adapter.out.mong.persistence.entity.MissionEntity;
import com.monglife.mongs.adapter.out.mong.persistence.repository.AccountMissionDetailRepository;
import com.monglife.mongs.adapter.out.mong.persistence.repository.AccountMissionRepository;
import com.monglife.mongs.adapter.out.mong.persistence.repository.MissionRepository;
import com.monglife.mongs.application.mong.port.out.MissionReadPort;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mission.model.Mission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MissionReadService implements MissionReadPort {

    private final MissionRepository missionRepository;

    private final AccountMissionRepository accountMissionRepository;

    private final AccountMissionDetailRepository accountMissionDetailRepository;

    /**
     * 활성 미션 마스터 목록 조회
     */
    @Override
    @Transactional
    public List<Mission> getActiveMissionsPort(MissionCycleCode cycleCode) {
        return missionRepository.findActiveByCycleCode(cycleCode).stream()
                .map(MissionEntity::toDomain)
                .toList();
    }

    /**
     * 미션 마스터 전체 목록 조회
     */
    @Override
    @Transactional
    public List<Mission> getMissionsPort() {
        return missionRepository.findAllWithRewards().stream()
                .map(MissionEntity::toDomain)
                .toList();
    }

    /**
     * 사용자 미션 목록 조회
     */
    @Override
    @Transactional
    public List<AccountMission> getAccountMissionsPort(Long accountId, Collection<String> cycleKeys) {

        List<AccountMissionEntity> accountMissionEntities =
                accountMissionRepository.findByAccountIdAndCycleKeys(accountId, cycleKeys);

        return toDomains(accountMissionEntities, accountMissionDetailRepository);
    }

    /**
     * DISTINCT 미션의 집계 대상까지 채워 도메인으로 변환.
     *
     * <p>사용자 미션 하나씩 detail 을 조회하면 N+1 이 된다. ID 를 모아 한 번에 읽는다.
     */
    static List<AccountMission> toDomains(List<AccountMissionEntity> accountMissionEntities, AccountMissionDetailRepository accountMissionDetailRepository) {

        if (accountMissionEntities.isEmpty()) {
            return List.of();
        }

        List<Long> accountMissionIds = accountMissionEntities.stream()
                .map(AccountMissionEntity::getAccountMissionId)
                .toList();

        Map<Long, Set<String>> detailCodesByAccountMissionId = new HashMap<>();
        for (AccountMissionDetailEntity detail : accountMissionDetailRepository.findByAccountMissionIdIn(accountMissionIds)) {
            detailCodesByAccountMissionId
                    .computeIfAbsent(detail.getAccountMissionId(), key -> new HashSet<>())
                    .add(detail.getDetailCode());
        }

        return accountMissionEntities.stream()
                .map(entity -> entity.toDomain(detailCodesByAccountMissionId.get(entity.getAccountMissionId())))
                .toList();
    }
}
