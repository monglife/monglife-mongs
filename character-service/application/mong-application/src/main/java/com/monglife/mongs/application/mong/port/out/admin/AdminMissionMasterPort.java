package com.monglife.mongs.application.mong.port.out.admin;

import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMissionCommand;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.model.Mission;

public interface AdminMissionMasterPort {

    Boolean isExistsMissionCodePort(String missionCode);

    /**
     * 같은 (액션, 목표 타입) 이 다른 주기에 이미 있는지.
     *
     * <p>겹침 방지의 등록 시점 검증이다. 일간 "밥 N번"이 있는데 주간에 "밥 M번"을 넣으려는 경우를 잡는다.
     * 같은 주기 안에서 난이도 단계를 여러 개 두는 것은 막지 않는다.
     */
    Boolean isExistsGoalInOtherCyclePort(MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, MissionCycleCode cycleCode);

    Mission createMissionPort(AdminCreateMissionCommand command);

    /** 미션과 그 리워드를 함께 지운다. 진행 중인 사용자 미션이 있으면 지우지 않는다 */
    Boolean deleteMissionPort(Long missionId);

    Boolean isExistsAccountMissionPort(Long missionId);
}
