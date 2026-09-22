package com.monglife.mongs.application.mong.port.in.admin.service;

import com.monglife.mongs.application.mong.port.exception.DuplicatedMissionGoalCountException;
import com.monglife.mongs.application.mong.port.exception.DuplicatedMissionGoalException;
import com.monglife.mongs.application.mong.port.exception.AlreadyExistsMissionCodeException;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMissionCommand;
import com.monglife.mongs.application.mong.port.out.MissionReadPort;
import com.monglife.mongs.application.mong.port.out.admin.AdminMissionMasterPort;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.enums.MissionRewardTypeCode;
import com.monglife.mongs.domain.mission.model.MissionReward;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 미션 등록 사전 검사 단위 테스트.
 *
 * <p>유니크 키 {@code uk_mission_goal} 은 (액션, 목표 타입, 목표치) 세 컬럼이고 <b>주기가 없다.</b>
 * 그래서 "다른 주기에 있는지" 검사만으로는 같은 주기 안의 목표치 충돌이 걸러지지 않고,
 * DB 제약이 그대로 터져 원시 500 이 나간다.
 */
class AdminMissionCreateTest {

    private final AdminMissionMasterPort adminMissionMasterPort = Mockito.mock(AdminMissionMasterPort.class);
    private final MissionReadPort missionReadPort = Mockito.mock(MissionReadPort.class);
    private final AdminMissionService adminMissionService = new AdminMissionService(adminMissionMasterPort, missionReadPort);

    private static AdminCreateMissionCommand command() {
        return AdminCreateMissionCommand.builder()
                .missionCode("MS_D_999")
                .cycleCode(MissionCycleCode.DAILY)
                .actionCode(MissionActionCode.FEED_FOOD)
                .goalTypeCode(MissionGoalTypeCode.COUNT)
                .title("밥 3번")
                .description("오늘 밥을 3번 주세요")
                .goalCount(3)
                .isActive(true)
                .sortOrder(999)
                .rewards(List.of(MissionReward.builder()
                        .rewardTypeCode(MissionRewardTypeCode.EXP)
                        .amount(10)
                        .build()))
                .build();
    }

    @Test
    @DisplayName("같은 주기에 목표치까지 같은 미션이 있으면 막는다. 예전에는 DB 제약이 터져 500 이 나갔다.")
    void createMissionWhenGoalCountDuplicated() {
        // arrange — 코드도 새롭고 교차 주기도 깨끗한데 목표치만 겹치는 상황
        Mockito.when(adminMissionMasterPort.isExistsMissionCodePort(Mockito.any())).thenReturn(false);
        Mockito.when(adminMissionMasterPort.isExistsGoalInOtherCyclePort(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        Mockito.when(adminMissionMasterPort.isExistsGoalPort(
                MissionActionCode.FEED_FOOD, MissionGoalTypeCode.COUNT, 3)).thenReturn(true);

        // act & assert
        assertThrows(DuplicatedMissionGoalCountException.class, () -> adminMissionService.createMissionUseCase(command()));
        Mockito.verify(adminMissionMasterPort, Mockito.never()).createMissionPort(Mockito.any());
    }

    @Test
    @DisplayName("교차 주기 검사가 먼저다. 두 경우의 안내 문구가 다르므로 코드가 섞이면 안 된다.")
    void createMissionWhenGoalExistsInOtherCycle() {
        // arrange
        Mockito.when(adminMissionMasterPort.isExistsMissionCodePort(Mockito.any())).thenReturn(false);
        Mockito.when(adminMissionMasterPort.isExistsGoalInOtherCyclePort(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

        // act & assert — 목표치 검사까지 가지 않는다
        assertThrows(DuplicatedMissionGoalException.class, () -> adminMissionService.createMissionUseCase(command()));
        Mockito.verify(adminMissionMasterPort, Mockito.never()).isExistsGoalPort(Mockito.any(), Mockito.any(), Mockito.anyInt());
    }

    @Test
    @DisplayName("코드 중복이 가장 먼저다.")
    void createMissionWhenCodeDuplicated() {
        // arrange
        Mockito.when(adminMissionMasterPort.isExistsMissionCodePort("MS_D_999")).thenReturn(true);

        // act & assert
        assertThrows(AlreadyExistsMissionCodeException.class, () -> adminMissionService.createMissionUseCase(command()));
        Mockito.verify(adminMissionMasterPort, Mockito.never()).isExistsGoalInOtherCyclePort(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("셋 다 깨끗하면 등록으로 넘어간다.")
    void createMission() {
        // arrange
        Mockito.when(adminMissionMasterPort.isExistsMissionCodePort(Mockito.any())).thenReturn(false);
        Mockito.when(adminMissionMasterPort.isExistsGoalInOtherCyclePort(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        Mockito.when(adminMissionMasterPort.isExistsGoalPort(Mockito.any(), Mockito.any(), Mockito.anyInt())).thenReturn(false);

        // act — createMissionPort 가 null 을 주면 toVo 에서 터지므로 호출 여부만 본다
        try {
            adminMissionService.createMissionUseCase(command());
        } catch (Exception ignored) {
            // 등록 포트의 반환값까지 꾸밀 필요는 없다
        }

        // assert
        Mockito.verify(adminMissionMasterPort).createMissionPort(Mockito.any());
    }
}
