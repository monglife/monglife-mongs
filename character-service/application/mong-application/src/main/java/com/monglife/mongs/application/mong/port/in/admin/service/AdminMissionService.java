package com.monglife.mongs.application.mong.port.in.admin.service;

import com.monglife.mongs.application.mong.port.exception.AlreadyExistsMissionCodeException;
import com.monglife.mongs.application.mong.port.exception.DuplicatedMissionGoalException;
import com.monglife.mongs.application.mong.port.exception.MissionInUseException;
import com.monglife.mongs.application.mong.port.exception.NotExistsMissionException;
import com.monglife.mongs.application.mong.port.in.admin.AdminMissionUseCase;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMissionCommand;
import com.monglife.mongs.application.mong.port.out.MissionReadPort;
import com.monglife.mongs.application.mong.port.out.admin.AdminMissionMasterPort;
import com.monglife.mongs.common.admin.log.AdminAuditLog;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mission.model.Mission;
import com.monglife.mongs.domain.mission.model.MissionReward;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMissionService implements AdminMissionUseCase {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final AdminMissionMasterPort adminMissionMasterPort;

    private final MissionReadPort missionReadPort;

    @Override
    @Transactional
    public List<Mission> getMissionsUseCase() {
        return missionReadPort.getMissionsPort();
    }

    /**
     * 미션 등록.
     *
     * <p>주기끼리 겹치지 않게 (액션, 목표 타입) 쌍을 한 주기에만 허용한다. 일간에 "밥 5번"(FEED_FOOD·COUNT)이
     * 있는데 주간에 "밥 20번"을 넣으면 사실상 같은 미션의 확대판이라 막는다. 같은 주기 안에서
     * 난이도 단계를 늘리는 것은 그대로 허용한다 - 일간 후보를 늘리는 정상 경로다.
     */
    @Override
    @Transactional
    public Mission createMissionUseCase(AdminCreateMissionCommand command) {

        if (Boolean.TRUE.equals(adminMissionMasterPort.isExistsMissionCodePort(command.getMissionCode()))) {
            throw new AlreadyExistsMissionCodeException();
        }

        if (Boolean.TRUE.equals(adminMissionMasterPort.isExistsGoalInOtherCyclePort(
                command.getActionCode(), command.getGoalTypeCode(), command.getCycleCode()))) {
            throw new DuplicatedMissionGoalException();
        }

        // 지급 시점이 아니라 등록 시점에 막는다. 운영자가 MAP 을 리워드로 넣으면
        // 사용자가 수령할 때가 되어서야 실패하는데, 그때는 이미 미션이 수령 처리된 뒤다.
        for (MissionReward reward : command.getRewards()) {
            reward.verify();
        }

        AdminAuditLog.write("mission created code={} cycle={} action={} goalType={} goal={} rewards={}",
                command.getMissionCode(), command.getCycleCode(), command.getActionCode(),
                command.getGoalTypeCode(), command.getGoalCount(), command.getRewards().size());

        return adminMissionMasterPort.createMissionPort(command);
    }

    /**
     * 미션 삭제.
     *
     * <p>진행 중인 사용자 미션이 걸려 있으면 지우지 않는다. 지우면 그 사용자의 화면에서 미션이
     * 사라지고 수령 대기 중이던 리워드도 같이 사라진다. 노출만 멈추려면 is_active 를 내리면 된다.
     */
    @Override
    @Transactional
    public void deleteMissionUseCase(Long missionId) {

        if (Boolean.TRUE.equals(adminMissionMasterPort.isExistsAccountMissionPort(missionId))) {
            throw new MissionInUseException();
        }

        if (!Boolean.TRUE.equals(adminMissionMasterPort.deleteMissionPort(missionId))) {
            throw new NotExistsMissionException();
        }

        AdminAuditLog.write("mission deleted missionId={}", missionId);
    }

    @Override
    @Transactional
    public List<AccountMission> getAccountMissionsUseCase(Long accountId) {

        LocalDate today = LocalDate.now(SERVICE_ZONE);

        List<String> cycleKeys = new ArrayList<>();
        for (MissionCycleCode cycleCode : MissionCycleCode.values()) {
            cycleKeys.add(cycleCode.cycleKey(today));
        }

        return missionReadPort.getAccountMissionsPort(accountId, cycleKeys);
    }
}
