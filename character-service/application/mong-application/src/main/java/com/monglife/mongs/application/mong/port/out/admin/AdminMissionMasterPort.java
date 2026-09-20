package com.monglife.mongs.application.mong.port.out.admin;

import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMissionCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMissionCommand;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.model.Mission;

import java.util.Optional;

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

    /** 미션 단건. 수정 전 현재 값을 보려고 쓴다 */
    Optional<Mission> getMissionPort(Long missionId);

    /** 목표치까지 같은 미션이 자기 말고 또 있는지 (uk_mission_goal 사전 검사) */
    Boolean isExistsGoalPort(MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, Integer goalCount, Long excludeMissionId);

    /**
     * 목표치까지 같은 미션이 이미 있는지 (등록용 uk_mission_goal 사전 검사).
     *
     * <p>위 메서드와 달리 제외할 ID 가 없다. 등록 시점에는 자기 ID 가 아직 없기 때문이다.
     */
    Boolean isExistsGoalPort(MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, Integer goalCount);

    /** 수정 가능한 값과 리워드를 바꾼다. 리워드는 통째 교체다 */
    Optional<Mission> updateMissionPort(AdminUpdateMissionCommand command);

    /** 노출 여부만 바꾼다. 없으면 빈 Optional */
    Optional<Mission> updateMissionActivePort(Long missionId, Boolean isActive);

    /** 미션과 그 리워드를 함께 지운다. 진행 중인 사용자 미션이 있으면 지우지 않는다 */
    Boolean deleteMissionPort(Long missionId);

    Boolean isExistsAccountMissionPort(Long missionId);
}
