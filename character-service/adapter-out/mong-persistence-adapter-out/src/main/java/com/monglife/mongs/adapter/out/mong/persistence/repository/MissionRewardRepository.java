package com.monglife.mongs.adapter.out.mong.persistence.repository;

import com.monglife.mongs.adapter.out.mong.persistence.entity.MissionRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionRewardRepository extends JpaRepository<MissionRewardEntity, Long> {

    List<MissionRewardEntity> findByMissionMissionId(Long missionId);

    void deleteByMissionMissionId(Long missionId);
}
