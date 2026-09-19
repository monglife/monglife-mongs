package com.monglife.mongs.adapter.out.mong.persistence.repository;

import com.monglife.mongs.adapter.out.mong.persistence.entity.MissionEntity;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<MissionEntity, Long> {

    /**
     * 활성 미션 마스터 조회. 리워드까지 한 번에 가져온다
     */
    @Query("""
            select distinct mission from MissionEntity mission
            left join fetch mission.rewards
            where mission.cycleCode = :cycleCode and mission.isActive = true
            order by mission.sortOrder asc
            """)
    List<MissionEntity> findActiveByCycleCode(@Param("cycleCode") MissionCycleCode cycleCode);

    /**
     * 미션 마스터 전체 조회 (관리자). 비활성도 포함한다
     */
    @Query("""
            select distinct mission from MissionEntity mission
            left join fetch mission.rewards
            order by mission.cycleCode asc, mission.sortOrder asc
            """)
    List<MissionEntity> findAllWithRewards();

    Optional<MissionEntity> findByMissionCode(String missionCode);

    boolean existsByMissionCode(String missionCode);

    /**
     * 같은 (액션, 목표 타입) 이 다른 주기에 이미 있는지.
     *
     * <p>겹침 방지의 등록 시점 검증이다. 일간 "밥 N번"이 있는데 주간에 "밥 M번"을 넣으려는 경우를 잡는다.
     * 같은 주기 안에서 난이도 단계를 여러 개 두는 것은 막지 않는다.
     */
    boolean existsByActionCodeAndGoalTypeCodeAndCycleCodeNot(MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, MissionCycleCode cycleCode);
}
