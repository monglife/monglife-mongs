package com.monglife.mongs.application.mong.port.in.admin;

import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMissionCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMissionCommand;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMissionVo;
import com.monglife.mongs.domain.mission.model.Mission;

import java.util.List;

public interface AdminMissionUseCase {

    /** 미션 마스터 전체 목록 (비활성 포함) */
    List<AdminMissionVo> getMissionsUseCase();

    /** 미션 등록 */
    AdminMissionVo createMissionUseCase(AdminCreateMissionCommand command);

    /** 미션 단건 (수정 모달이 현재 값을 읽는다) */
    AdminMissionVo getMissionUseCase(Long missionId);

    /**
     * 미션 수정.
     *
     * <p>제목·설명·목표치·정렬·노출·리워드만 바꾼다. 코드·주기·액션·목표 타입은 정체성이라 고정이다.
     */
    AdminMissionVo updateMissionUseCase(AdminUpdateMissionCommand command);

    /**
     * 미션 노출 전환.
     *
     * <p>삭제와 달리 진행 중인 사용자가 있어도 막지 않는다 - 이미 적재된 주기는 그대로 두고
     * 다음 주기 선정에서만 빠지기 때문이다. 진행 중인 미션을 즉시 감출 수단은 없다(그게 맞다).
     */
    AdminMissionVo updateMissionActiveUseCase(Long missionId, Boolean isActive);

    /** 미션 삭제 */
    void deleteMissionUseCase(Long missionId);

    /** 특정 계정의 이번 주기 미션 진행 현황 (운영 문의 대응) */
    List<AccountMission> getAccountMissionsUseCase(Long accountId);
}
