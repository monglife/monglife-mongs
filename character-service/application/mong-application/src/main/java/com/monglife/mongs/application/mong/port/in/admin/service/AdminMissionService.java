package com.monglife.mongs.application.mong.port.in.admin.service;

import com.monglife.mongs.application.mong.port.exception.AlreadyExistsMissionCodeException;
import com.monglife.mongs.application.mong.port.exception.DuplicatedMissionGoalException;
import com.monglife.mongs.application.mong.port.exception.MissionInUseException;
import com.monglife.mongs.application.mong.port.exception.MissionPublishedException;
import com.monglife.mongs.application.mong.port.exception.NotExistsMissionException;
import com.monglife.mongs.application.mong.port.in.admin.AdminMissionUseCase;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMissionCommand;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMissionVo;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMissionCommand;
import com.monglife.mongs.application.mong.port.out.MissionReadPort;
import com.monglife.mongs.application.mong.port.out.admin.AdminMissionMasterPort;
import com.monglife.mongs.common.admin.log.AdminAuditLog;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mission.model.Mission;
import com.monglife.mongs.domain.mission.model.MissionReward;
import com.monglife.mongs.domain.mission.model.MissionRotation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminMissionService implements AdminMissionUseCase {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final AdminMissionMasterPort adminMissionMasterPort;

    private final MissionReadPort missionReadPort;

    @Override
    @Transactional
    public List<AdminMissionVo> getMissionsUseCase() {

        LocalDate today = LocalDate.now(SERVICE_ZONE);

        // 주기마다 활성 목록을 한 번씩만 읽어 당첨 그룹을 구한다. 미션마다 다시 읽으면 N+1 이다.
        Map<MissionCycleCode, Integer> currentGroups = new EnumMap<>(MissionCycleCode.class);
        for (MissionCycleCode cycleCode : MissionCycleCode.values()) {
            Integer group = MissionRotation.currentGroup(missionReadPort.getActiveMissionsPort(cycleCode), cycleCode, today);
            if (group != null) {
                currentGroups.put(cycleCode, group);
            }
        }

        return missionReadPort.getMissionsPort().stream()
                .map(mission -> this.toVo(mission, currentGroups.get(mission.getCycleCode()), today))
                .toList();
    }

    /** 게시 여부는 로테이션 규칙과 같은 판정을 쓴다 - 화면 표시와 잠금이 어긋나면 안 된다 */
    private AdminMissionVo toVo(Mission mission, Integer currentGroup, LocalDate today) {

        boolean published = Boolean.TRUE.equals(mission.getIsActive())
                && (MissionCycleCode.DAILY.equals(mission.getCycleCode())
                        || mission.getRotationGroup().equals(currentGroup));

        return AdminMissionVo.builder()
                .mission(mission)
                .isPublished(published)
                .periodStart(mission.getCycleCode().periodStart(today))
                .periodEnd(mission.getCycleCode().periodEnd(today))
                .currentRotationGroup(currentGroup)
                .build();
    }

    /** 단건용. 그 주기 활성 목록을 한 번 읽어 당첨 그룹을 구한다 */
    private AdminMissionVo toVo(Mission mission) {

        LocalDate today = LocalDate.now(SERVICE_ZONE);
        Integer currentGroup = MissionRotation.currentGroup(
                missionReadPort.getActiveMissionsPort(mission.getCycleCode()), mission.getCycleCode(), today);

        return this.toVo(mission, currentGroup, today);
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
    public AdminMissionVo createMissionUseCase(AdminCreateMissionCommand command) {

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

        return this.toVo(adminMissionMasterPort.createMissionPort(command));
    }

    @Override
    @Transactional
    public AdminMissionVo getMissionUseCase(Long missionId) {
        return this.toVo(adminMissionMasterPort.getMissionPort(missionId).orElseThrow(NotExistsMissionException::new));
    }

    /**
     * 미션 수정.
     *
     * <p>바꿀 수 있는 것은 제목·설명·목표치·정렬·노출·리워드다. 코드·주기·액션·목표 타입은 고정이다 -
     * 바꾸면 이미 적재된 사용자 미션의 진행도가 다른 의미의 숫자가 된다. 그런 변경이 필요하면
     * 새 미션을 등록하고 옛 미션을 비활성으로 내린다.
     *
     * <p>이미 적재된 사용자 미션은 이 미션을 참조만 하므로 제목·목표치·리워드 변경이 **진행 중인
     * 사용자에게도 즉시 반영된다.** 목표치를 내리면 그 순간 달성 처리되는 사용자가 생기고,
     * 올리면 달성했던 사용자가 다시 미달성이 된다. 수령이 끝난 건은 영향이 없다.
     */
    @Override
    @Transactional
    public AdminMissionVo updateMissionUseCase(AdminUpdateMissionCommand command) {

        Mission current = adminMissionMasterPort.getMissionPort(command.getMissionId())
                .orElseThrow(NotExistsMissionException::new);

        this.verifyNotPublished(current);

        // uk_mission_goal (액션, 목표 타입, 목표치) 사전 검사. DB 제약이 터지기 전에 잡는다.
        if (!current.getGoalCount().equals(command.getGoalCount())
                && Boolean.TRUE.equals(adminMissionMasterPort.isExistsGoalPort(
                        current.getActionCode(), current.getGoalTypeCode(), command.getGoalCount(), command.getMissionId()))) {
            throw new DuplicatedMissionGoalException();
        }

        for (MissionReward reward : command.getRewards()) {
            reward.verify();
        }

        Mission mission = adminMissionMasterPort.updateMissionPort(command)
                .orElseThrow(NotExistsMissionException::new);
        AdminMissionVo vo = this.toVo(mission);

        AdminAuditLog.write("mission updated missionId={} code={} title={} goal={}->{} rewards={}",
                command.getMissionId(), current.getMissionCode(), command.getTitle(),
                current.getGoalCount(), command.getGoalCount(), command.getRewards().size());

        return vo;
    }

    /**
     * 게시 중이면 막는다.
     *
     * <p>게시 중 = 지금 사용자 화면에 떠 있고 진행도가 쌓이는 중이다. 이때 제목이나 목표치를
     * 갈아 끼우면 이미 그 미션을 받아 둔 사용자에게 곧바로 반영되어, 하던 미션이 말없이 바뀐다.
     *
     * <p>푸는 길은 둘이다 - 노출을 내리거나(즉시), 다음 주기를 기다린다(로테이션이 넘어간다).
     */
    private void verifyNotPublished(Mission mission) {

        if (MissionRotation.isPublished(
                mission,
                missionReadPort.getActiveMissionsPort(mission.getCycleCode()),
                LocalDate.now(SERVICE_ZONE))) {
            throw new MissionPublishedException();
        }
    }

    /**
     * 미션 노출 전환.
     *
     * <p>삭제가 막히는 자리를 메우는 경로다. 진행 중인 사용자 미션이 있으면 삭제는 거절되는데,
     * 그때도 다음 주기부터 노출을 멈출 수단이 필요하다.
     *
     * <p>게시 중이어도 막지 않는다. 수정·삭제와 달리 이건 **유일한 비상구**다 - 문제가 생긴
     * 미션을 즉시 내릴 수 없으면 주기가 끝날 때까지 손을 못 댄다. 내리면 게시가 풀려 수정도 열린다.
     *
     * <p>이미 적재된 사용자 미션은 건드리지 않는다. {@code syncAccountMissions} 는 그 주기에
     * 행이 하나라도 있으면 다시 뽑지 않으므로, 진행 중이던 사용자는 주기가 끝날 때까지 그대로
     * 보고 수령도 한다. 수령 대기 중이던 리워드가 사라지지 않는다는 뜻이다.
     */
    @Override
    @Transactional
    public AdminMissionVo updateMissionActiveUseCase(Long missionId, Boolean isActive) {

        Mission mission = adminMissionMasterPort.updateMissionActivePort(missionId, isActive)
                .orElseThrow(NotExistsMissionException::new);

        AdminAuditLog.write("mission active changed missionId={} code={} isActive={}",
                missionId, mission.getMissionCode(), isActive);

        return this.toVo(mission);
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

        this.verifyNotPublished(adminMissionMasterPort.getMissionPort(missionId)
                .orElseThrow(NotExistsMissionException::new));

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
