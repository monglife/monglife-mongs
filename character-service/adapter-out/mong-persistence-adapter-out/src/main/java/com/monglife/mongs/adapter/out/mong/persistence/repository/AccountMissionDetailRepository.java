package com.monglife.mongs.adapter.out.mong.persistence.repository;

import com.monglife.mongs.adapter.out.mong.persistence.entity.AccountMissionDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AccountMissionDetailRepository extends JpaRepository<AccountMissionDetailEntity, Long> {

    List<AccountMissionDetailEntity> findByAccountMissionIdIn(Collection<Long> accountMissionIds);
}
