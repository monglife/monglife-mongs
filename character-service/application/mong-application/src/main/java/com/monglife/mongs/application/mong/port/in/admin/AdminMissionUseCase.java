package com.monglife.mongs.application.mong.port.in.admin;

import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMissionCommand;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mission.model.Mission;

import java.util.List;

public interface AdminMissionUseCase {

    /** 미션 마스터 전체 목록 (비활성 포함) */
    List<Mission> getMissionsUseCase();

    /** 미션 등록 */
    Mission createMissionUseCase(AdminCreateMissionCommand command);

    /** 미션 삭제 */
    void deleteMissionUseCase(Long missionId);

    /** 특정 계정의 이번 주기 미션 진행 현황 (운영 문의 대응) */
    List<AccountMission> getAccountMissionsUseCase(Long accountId);
}
