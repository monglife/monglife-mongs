package com.monglife.mongs.adapter.in.admin.character.web.controller;

import com.monglife.core.dto.response.PageResponseDto;
import com.monglife.core.dto.response.ResponseDto;
import com.monglife.core.vo.page.PageResult;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.character.web.dto.request.AdminInventoryGrantRequestDto;
import com.monglife.mongs.adapter.in.admin.character.web.dto.request.AdminMongSleepRequestDto;
import com.monglife.mongs.adapter.in.admin.character.web.dto.request.AdminMongStateRequestDto;
import com.monglife.mongs.adapter.in.admin.character.web.dto.request.AdminMongStatusRequestDto;
import com.monglife.mongs.adapter.in.admin.character.web.dto.request.AdminTaskCreateRequestDto;
import com.monglife.mongs.adapter.in.admin.character.web.dto.response.*;
import com.monglife.mongs.adapter.in.admin.character.web.enums.AdapterInAdminCharacterWebResponse;
import com.monglife.mongs.adapter.in.admin.character.web.util.AdminPage;
import com.monglife.mongs.adapter.in.admin.character.web.util.PageQuery;
import com.monglife.mongs.application.mong.port.in.admin.AdminMongUseCase;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminGetMongsCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMongSleepCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMongStateCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMongStatusCommand;
import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import com.monglife.mongs.domain.mong.model.Inventory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/admin/mongs")
@RequiredArgsConstructor
public class AdminMongController {

    private static final Set<String> SORT_KEYS = Set.of("mongId", "createdAt", "exp", "payPoint", "accountId");

    private final AdminMongUseCase adminMongUseCase;

    @EntryLoggingPoint
    @GetMapping
    public ResponseEntity<PageResponseDto<List<AdminMongResponseDto>>> getMongs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) MongStateCode stateCode,
            @RequestParam(required = false) MongStatusCode statusCode,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort
    ) {
        AdminGetMongsCommand command = AdminGetMongsCommand.builder()
                .pageRequest(PageQuery.of(page, size, sort, SORT_KEYS, "mongId", true))
                .accountId(accountId)
                .stateCode(stateCode)
                .statusCode(statusCode)
                .query(PageQuery.blankToNull(query))
                .build();

        return AdminPage.toResponse(AdapterInAdminCharacterWebResponse.GET_MONGS, adminMongUseCase.getMongsUseCase(command), AdminMongResponseDto::of);
    }

    @EntryLoggingPoint
    @GetMapping("/{mongId}")
    public ResponseEntity<ResponseDto<AdminMongResponseDto>> getMong(@PathVariable Long mongId) {
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_MONG.toResponseDto(AdminMongResponseDto.of(adminMongUseCase.getMongUseCase(mongId))));
    }

    @EntryLoggingPoint
    @PatchMapping("/{mongId}/status")
    public ResponseEntity<ResponseDto<AdminMongResponseDto>> updateStatus(
            @PathVariable Long mongId,
            @Valid @RequestBody AdminMongStatusRequestDto requestDto
    ) {
        AdminUpdateMongStatusCommand command = AdminUpdateMongStatusCommand.builder()
                .mongId(mongId)
                .weight(requestDto.getWeight())
                .strength(requestDto.getStrength())
                .satiety(requestDto.getSatiety())
                .healthy(requestDto.getHealthy())
                .fatigue(requestDto.getFatigue())
                .exp(requestDto.getExp())
                .payPoint(requestDto.getPayPoint())
                .poopCount(requestDto.getPoopCount())
                .randomDrawTicketCount(requestDto.getRandomDrawTicketCount())
                .build();

        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.UPDATE_MONG_STATUS.toResponseDto(AdminMongResponseDto.of(adminMongUseCase.updateMongStatusUseCase(command))));
    }

    /** 수면·기상 전환. 지수 증감 스케줄도 앱에서 누른 것과 같이 교체된다 */
    @EntryLoggingPoint
    @PatchMapping("/{mongId}/sleep")
    public ResponseEntity<ResponseDto<AdminMongResponseDto>> updateSleep(
            @PathVariable Long mongId,
            @Valid @RequestBody AdminMongSleepRequestDto requestDto
    ) {
        AdminUpdateMongSleepCommand command = AdminUpdateMongSleepCommand.builder()
                .mongId(mongId)
                .isSleep(requestDto.getIsSleep())
                .build();

        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.UPDATE_MONG_SLEEP.toResponseDto(AdminMongResponseDto.of(adminMongUseCase.updateMongSleepUseCase(command))));
    }

    @EntryLoggingPoint
    @PatchMapping("/{mongId}/state")
    public ResponseEntity<ResponseDto<AdminMongResponseDto>> updateState(
            @PathVariable Long mongId,
            @Valid @RequestBody AdminMongStateRequestDto requestDto
    ) {
        AdminUpdateMongStateCommand command = AdminUpdateMongStateCommand.builder()
                .mongId(mongId)
                .stateCode(requestDto.getStateCode())
                .build();

        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.UPDATE_MONG_STATE.toResponseDto(AdminMongResponseDto.of(adminMongUseCase.updateMongStateUseCase(command))));
    }

    @EntryLoggingPoint
    @DeleteMapping("/{mongId}")
    public ResponseEntity<ResponseDto<AdminMongResponseDto>> deleteMong(@PathVariable Long mongId) {
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.DELETE_MONG.toResponseDto(AdminMongResponseDto.of(adminMongUseCase.deleteMongUseCase(mongId))));
    }

    @EntryLoggingPoint
    @GetMapping("/{mongId}/tasks")
    public ResponseEntity<ResponseDto<List<AdminTaskResponseDto>>> getTasks(@PathVariable Long mongId) {
        List<AdminTaskResponseDto> items = adminMongUseCase.getTasksUseCase(mongId).stream().map(AdminTaskResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_TASKS.toResponseDto(items));
    }

    @EntryLoggingPoint
    @PostMapping("/tasks/{taskId}/pause")
    public ResponseEntity<ResponseDto<AdminTaskResponseDto>> pauseTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.PAUSE_TASK.toResponseDto(AdminTaskResponseDto.of(adminMongUseCase.pauseTaskUseCase(taskId))));
    }

    @EntryLoggingPoint
    @PostMapping("/tasks/{taskId}/resume")
    public ResponseEntity<ResponseDto<AdminTaskResponseDto>> resumeTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.RESUME_TASK.toResponseDto(AdminTaskResponseDto.of(adminMongUseCase.resumeTaskUseCase(taskId))));
    }

    @EntryLoggingPoint
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<ResponseDto<AdminTaskResponseDto>> deleteTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.DELETE_TASK.toResponseDto(AdminTaskResponseDto.of(adminMongUseCase.deleteTaskUseCase(taskId))));
    }

    /** 스케줄 등록. 수면·기상 시각은 몽에 저장된 값을 쓰므로 따로 받지 않는다 */
    @EntryLoggingPoint
    @PostMapping("/{mongId}/tasks")
    public ResponseEntity<ResponseDto<AdminTaskResponseDto>> createTask(
            @PathVariable Long mongId,
            @Valid @RequestBody AdminTaskCreateRequestDto requestDto
    ) {
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.CREATE_TASK
                .toResponseDto(AdminTaskResponseDto.of(adminMongUseCase.createTaskUseCase(mongId, requestDto.getSchedulerTypeCode()))));
    }

    @EntryLoggingPoint
    @GetMapping("/{mongId}/inventories")
    public ResponseEntity<PageResponseDto<List<AdminInventoryResponseDto>>> getInventories(
            @PathVariable Long mongId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        // 앱용 인벤토리 조회는 1 부터 시작하는 페이지를 쓴다. 관리자 웹의 0-based 를 그대로 맞춰 준다.
        int adminPage = PageQuery.page(page);
        int adminSize = PageQuery.size(size);

        PageResult<Inventory> pageResult = adminMongUseCase.getInventoriesUseCase(mongId, adminPage + 1, adminSize);

        List<AdminInventoryResponseDto> items = pageResult.getResult().stream().map(AdminInventoryResponseDto::of).toList();

        return ResponseEntity.ok()
                .header(AdminPage.TOTAL_COUNT_HEADER, String.valueOf((long) pageResult.getTotalPage() * adminSize))
                .body(AdapterInAdminCharacterWebResponse.GET_INVENTORIES.toPageResponseDto(
                        items, adminPage, adminSize, pageResult.getTotalPage(), pageResult.getIsLastPage()));
    }

    @EntryLoggingPoint
    @PostMapping("/{mongId}/inventories")
    public ResponseEntity<ResponseDto<AdminInventoryResponseDto>> grantInventory(
            @PathVariable Long mongId,
            @Valid @RequestBody AdminInventoryGrantRequestDto requestDto
    ) {
        AdminInventoryResponseDto dto = AdminInventoryResponseDto.of(
                adminMongUseCase.grantInventoryUseCase(mongId, requestDto.getInventoryCode(), requestDto.getInventoryTypeCode()));
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GRANT_INVENTORY.toResponseDto(dto));
    }
}
