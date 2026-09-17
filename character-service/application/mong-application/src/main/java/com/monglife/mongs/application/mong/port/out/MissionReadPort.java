package com.monglife.mongs.application.mong.port.out;

import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mission.model.Mission;

import java.util.Collection;
import java.util.List;

public interface MissionReadPort {

    /**
     * 활성 미션 마스터 목록 조회
     * @param cycleCode 미션 주기
     * @return 리워드까지 채워진 미션 마스터 목록
     */
    List<Mission> getActiveMissionsPort(MissionCycleCode cycleCode);

    /**
     * 미션 마스터 전체 목록 조회 (관리자)
     * @return 비활성 포함 전체 미션 마스터 목록
     */
    List<Mission> getMissionsPort();

    /**
     * 사용자 미션 목록 조회
     * @param accountId 계정 ID
     * @param cycleKeys 조회할 주기 키 목록 (오늘·이번 주·이번 달)
     * @return 사용자 미션 목록
     */
    List<AccountMission> getAccountMissionsPort(Long accountId, Collection<String> cycleKeys);
}
