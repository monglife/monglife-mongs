package com.monglife.mongs.adapter.out.mong.persistence.repository;

import com.monglife.mongs.adapter.out.mong.persistence.entity.AccountMissionEntity;
import com.monglife.mongs.adapter.out.mong.persistence.repository.dsl.AccountMissionDslRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AccountMissionRepository extends JpaRepository<AccountMissionEntity, Long>, AccountMissionDslRepository {

    @Query("""
            select distinct accountMission from AccountMissionEntity accountMission
            join fetch accountMission.mission mission
            left join fetch mission.rewards
            where accountMission.accountId = :accountId and accountMission.cycleKey in :cycleKeys
            order by mission.cycleCode asc, mission.sortOrder asc
            """)
    List<AccountMissionEntity> findByAccountIdAndCycleKeys(@Param("accountId") Long accountId, @Param("cycleKeys") Collection<String> cycleKeys);

    /** 미션 삭제 가드. 사용자에게 한 번이라도 적재된 미션은 지우지 않는다 */
    boolean existsByMissionMissionId(Long missionId);

    /**
     * 사용자 미션 적재.
     *
     * <p>JPA save 가 아니라 INSERT IGNORE 다. 앱이 기동하며 미션 조회와 플레이 동작을 동시에 던지면
     * 같은 (계정, 미션, 주기) 를 두 요청이 같이 넣으려 한다. 유니크 키 위반으로 예외가 나면
     * 그 트랜잭션이 rollback-only 가 되어 정작 밥 주기까지 실패한다. 뒤에 온 쪽은 조용히 버린다.
     *
     * <p>네이티브라 Auditing 리스너를 타지 않아 시각을 직접 넣는다.
     * MySQL 문법이고 H2 는 MODE=MySQL 에서 받아 준다 - 시드 SQL 과 같은 전제다.
     */
    @Modifying
    @Query(value = """
            INSERT IGNORE INTO mongs_account_mission
                (account_id, mission_id, mission_cycle_code, cycle_key, progress_count, state_code, created_at, updated_at)
            VALUES (:accountId, :missionId, :cycleCode, :cycleKey, 0, 'IN_PROGRESS', NOW(), NOW())
            """, nativeQuery = true)
    int insertIgnore(
            @Param("accountId") Long accountId,
            @Param("missionId") Long missionId,
            @Param("cycleCode") String cycleCode,
            @Param("cycleKey") String cycleKey
    );
}
