package com.monglife.mongs.application.mong.port.out;

import com.monglife.mongs.application.mong.port.out.vo.CreateAccountMissionVo;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.model.AccountMission;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MissionPersistencePort {

    /**
     * 사용자 미션 적재.
     *
     * <p>이미 있는 행은 조용히 건너뛴다. 첫 조회가 동시에 들어와도 한쪽이 유니크 키 위반으로
     * 죽으면 안 된다 - 그 예외가 트랜잭션을 rollback-only 로 만들어 정작 플레이 동작까지 막는다.
     *
     * @param createAccountMissionVos 적재할 사용자 미션 목록
     * @return 실제로 새로 들어간 행 수
     */
    int createAccountMissionsPort(List<CreateAccountMissionVo> createAccountMissionVos);

    /**
     * 진행도 갱신 대상 사용자 미션 조회 (행 잠금).
     *
     * <p>DISTINCT 미션의 진행도는 detail 행 수라, 같은 계정의 동시 요청이 각자 읽고 각자 쓰면
     * 하나가 덮여 사라진다. 갱신할 행만 잠가서 막는다.
     *
     * @param accountId 계정 ID
     * @param cycleKeys 현재 주기 키 목록
     * @param actionCodes 대상 액션 코드 목록
     * @return 진행 중인 사용자 미션 목록
     */
    List<AccountMission> getAccountMissionsForUpdatePort(Long accountId, Collection<String> cycleKeys, Collection<MissionActionCode> actionCodes);

    /**
     * 사용자 미션 조회 (행 잠금). 리워드 수령용
     * @param accountMissionId 사용자 미션 ID
     * @return 사용자 미션 도메인 객체
     */
    Optional<AccountMission> getAccountMissionForUpdatePort(Long accountMissionId);

    /**
     * 사용자 미션 상태 동기화. DISTINCT 미션이면 새로 집계된 대상 코드도 함께 적재한다
     * @param accountMission 사용자 미션 도메인 객체
     */
    void saveAccountMissionPort(AccountMission accountMission);
}
