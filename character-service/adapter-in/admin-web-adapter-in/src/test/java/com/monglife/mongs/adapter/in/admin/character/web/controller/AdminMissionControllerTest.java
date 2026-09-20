package com.monglife.mongs.adapter.in.admin.character.web.controller;

import com.monglife.mongs.adapter.in.admin.character.web.exception.AdminCharacterExceptionHandler;
import com.monglife.mongs.application.mong.port.in.admin.AdminMissionUseCase;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMissionVo;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.model.Mission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 미션 응답 직렬화 테스트.
 *
 * <p>{@code periodStart}/{@code periodEnd} 는 이 프로젝트에서 유일하게 {@code @JsonFormat} 이
 * 빠져 있던 날짜 필드라 {@code [2026,9,20]} 배열로 나갔다. 화면에는 "2026,9,20 ~ 2026,9,20"
 * 으로 보였다.
 *
 * <p>직접 만든 {@code ObjectMapper} 가 아니라 MVC 메시지 컨버터를 통해 확인한다 -
 * 맨 {@code new ObjectMapper()} 로는 통과하면서 운영에서 깨질 수 있다.
 */
class AdminMissionControllerTest {

    private final AdminMissionUseCase adminMissionUseCase = Mockito.mock(AdminMissionUseCase.class);

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AdminMissionController(adminMissionUseCase))
            .setControllerAdvice(new AdminCharacterExceptionHandler())
            .build();

    @Test
    @DisplayName("게시 기간이 yyyy-MM-dd 문자열로 나간다. 배열이면 화면이 숫자를 늘어놓는다.")
    void periodIsSerializedAsString() throws Exception {
        // arrange
        Mission mission = Mission.builder()
                .missionId(1L)
                .missionCode("MS_D_001")
                .cycleCode(MissionCycleCode.DAILY)
                .actionCode(MissionActionCode.FEED_FOOD)
                .goalTypeCode(MissionGoalTypeCode.COUNT)
                .title("밥 3번")
                .description("오늘 밥을 3번 주세요")
                .goalCount(3)
                .isActive(true)
                .sortOrder(1)
                .rotationGroup(0)
                .rewards(List.of())
                .build();

        Mockito.when(adminMissionUseCase.getMissionsUseCase()).thenReturn(List.of(AdminMissionVo.builder()
                .mission(mission)
                .isPublished(true)
                .periodStart(LocalDate.of(2026, 9, 20))
                .periodEnd(LocalDate.of(2026, 9, 20))
                .currentRotationGroup(0)
                .build()));

        // act & assert
        mockMvc.perform(get("/admin/missions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].periodStart").value("2026-09-20"))
                .andExpect(jsonPath("$.result[0].periodEnd").value("2026-09-20"));
    }
}
