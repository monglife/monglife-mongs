package com.monglife.mongs.adapter.out.mong.persistence.repository.dsl;

import com.monglife.mongs.adapter.out.mong.persistence.entity.AccountMissionEntity;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountMissionDslRepository {

    List<AccountMissionEntity> findForUpdate(Long accountId, Collection<String> cycleKeys, Collection<MissionActionCode> actionCodes);

    Optional<AccountMissionEntity> findByIdWithLock(Long accountMissionId);
}
