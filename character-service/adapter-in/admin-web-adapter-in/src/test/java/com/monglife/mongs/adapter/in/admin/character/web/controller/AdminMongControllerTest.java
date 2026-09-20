package com.monglife.mongs.adapter.in.admin.character.web.controller;

import com.monglife.mongs.adapter.in.admin.character.web.exception.AdminCharacterExceptionHandler;
import com.monglife.mongs.application.mong.port.enums.MongSchedulerType;
import com.monglife.mongs.application.mong.port.in.admin.AdminMongUseCase;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminTaskVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 스케줄 등록 요청 바인딩 테스트.
 *
 * <p>목록 응답은 코드(DECREASE-STATUS)를 주는데 요청 DTO 가 enum 이라 Jackson 이 이름으로
 * 바인딩하던 문제를 막는다. 그때는 올바른 코드와 존재하지 않는 코드의 응답이 똑같았다 -
 * 둘 다 응답 코드 없는 Spring 기본 봉투였다.
 */
class AdminMongControllerTest {

    private static final Long MONG_ID = 2L;

    private final AdminMongUseCase adminMongUseCase = Mockito.mock(AdminMongUseCase.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminMongController(adminMongUseCase))
                .setControllerAdvice(new AdminCharacterExceptionHandler())
                .build();

        Mockito.when(adminMongUseCase.createTaskUseCase(Mockito.eq(MONG_ID), Mockito.any()))
                .thenReturn(AdminTaskVo.builder()
                        .taskId(1L)
                        .mongId(MONG_ID)
                        .accountId(1L)
                        .schedulerTypeCode("DECREASE-STATUS")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
    }

    private void createTask(String schedulerTypeCode, int expectedStatus) throws Exception {
        mockMvc.perform(post("/admin/mongs/{mongId}/tasks", MONG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"schedulerTypeCode\":\"" + schedulerTypeCode + "\"}"))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    @DisplayName("목록이 돌려준 코드로 등록된다. 7종 전부 통한다.")
    void createTaskWithCode() throws Exception {
        for (MongSchedulerType type : MongSchedulerType.values()) {
            createTask(type.getCode(), 200);
        }

        ArgumentCaptor<MongSchedulerType> captor = ArgumentCaptor.forClass(MongSchedulerType.class);
        Mockito.verify(adminMongUseCase, Mockito.times(MongSchedulerType.values().length))
                .createTaskUseCase(Mockito.eq(MONG_ID), captor.capture());

        assertEquals(MongSchedulerType.values().length, captor.getAllValues().size());
    }

    @Test
    @DisplayName("enum 이름으로도 등록된다. 관리자 웹 배포가 늦어도 깨지지 않는다.")
    void createTaskWithEnumName() throws Exception {
        createTask("DECREASE_STATUS", 200);

        Mockito.verify(adminMongUseCase).createTaskUseCase(MONG_ID, MongSchedulerType.DECREASE_STATUS);
    }

    @Test
    @DisplayName("코드와 이름이 같은 타입으로 풀린다.")
    void createTaskResolvesToSameType() throws Exception {
        createTask("DECREASE-STATUS", 200);

        Mockito.verify(adminMongUseCase).createTaskUseCase(MONG_ID, MongSchedulerType.DECREASE_STATUS);
    }

    @Test
    @DisplayName("알 수 없는 코드는 응답 코드가 담긴 봉투로 거절한다. 맨 Spring 400 이 아니다.")
    void createTaskWithUnknownCode() throws Exception {
        mockMvc.perform(post("/admin/mongs/{mongId}/tasks", MONG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"schedulerTypeCode\":\"NOPE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400-101-106"));

        Mockito.verify(adminMongUseCase, Mockito.never()).createTaskUseCase(Mockito.any(), Mockito.any());
    }

    @Test
    @DisplayName("본문이 깨져도 응답 코드가 담긴 봉투로 답한다.")
    void createTaskWithBrokenBody() throws Exception {
        mockMvc.perform(post("/admin/mongs/{mongId}/tasks", MONG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").exists());
    }
}
