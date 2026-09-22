package com.monglife.mongs.adapter.in.admin.user.web.controller;

import com.monglife.core.dto.response.PageResponseDto;
import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.user.web.dto.request.AdminCollectionGrantRequestDto;
import com.monglife.mongs.adapter.in.admin.user.web.dto.request.AdminSlotCountRequestDto;
import com.monglife.mongs.adapter.in.admin.user.web.dto.request.AdminStarPointRequestDto;
import com.monglife.mongs.adapter.in.admin.user.web.dto.response.AdminCollectionMapResponseDto;
import com.monglife.mongs.adapter.in.admin.user.web.dto.response.AdminCollectionMongResponseDto;
import com.monglife.mongs.adapter.in.admin.user.web.dto.response.AdminMemberResponseDto;
import com.monglife.mongs.adapter.in.admin.user.web.enums.AdapterInAdminUserWebResponse;
import com.monglife.mongs.adapter.in.admin.user.web.util.AdminPage;
import com.monglife.mongs.adapter.in.admin.user.web.util.PageQuery;
import com.monglife.mongs.application.member.port.in.admin.AdminMemberUseCase;
import com.monglife.mongs.application.member.port.in.admin.command.AdminAdjustStarPointCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminGetMembersCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminUpdateSlotCountCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private static final Set<String> SORT_KEYS = Set.of("accountId", "starPoint", "slotCount", "createdAt");

    private final AdminMemberUseCase adminMemberUseCase;

    @EntryLoggingPoint
    @GetMapping
    public ResponseEntity<PageResponseDto<List<AdminMemberResponseDto>>> getMembers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String sort
    ) {
        AdminGetMembersCommand command = AdminGetMembersCommand.builder()
                .pageRequest(PageQuery.of(page, size, sort, SORT_KEYS, "accountId", true))
                .accountId(accountId)
                .build();

        return AdminPage.toResponse(AdapterInAdminUserWebResponse.GET_MEMBERS, adminMemberUseCase.getMembersUseCase(command), AdminMemberResponseDto::of);
    }

    @EntryLoggingPoint
    @GetMapping("/{accountId}")
    public ResponseEntity<ResponseDto<AdminMemberResponseDto>> getMember(@PathVariable Long accountId) {
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.GET_MEMBER.toResponseDto(AdminMemberResponseDto.of(adminMemberUseCase.getMemberUseCase(accountId))));
    }

    @EntryLoggingPoint
    @PatchMapping("/{accountId}/star-point")
    public ResponseEntity<ResponseDto<AdminMemberResponseDto>> adjustStarPoint(
            @PathVariable Long accountId,
            @Valid @RequestBody AdminStarPointRequestDto requestDto
    ) {
        AdminAdjustStarPointCommand command = AdminAdjustStarPointCommand.builder()
                .accountId(accountId)
                .delta(requestDto.getDelta())
                .build();

        return ResponseEntity.ok(AdapterInAdminUserWebResponse.ADJUST_STAR_POINT.toResponseDto(AdminMemberResponseDto.of(adminMemberUseCase.adjustStarPointUseCase(command))));
    }

    @EntryLoggingPoint
    @PatchMapping("/{accountId}/slot-count")
    public ResponseEntity<ResponseDto<AdminMemberResponseDto>> updateSlotCount(
            @PathVariable Long accountId,
            @Valid @RequestBody AdminSlotCountRequestDto requestDto
    ) {
        AdminUpdateSlotCountCommand command = AdminUpdateSlotCountCommand.builder()
                .accountId(accountId)
                .slotCount(requestDto.getSlotCount())
                .build();

        return ResponseEntity.ok(AdapterInAdminUserWebResponse.UPDATE_SLOT_COUNT.toResponseDto(AdminMemberResponseDto.of(adminMemberUseCase.updateSlotCountUseCase(command))));
    }

    @EntryLoggingPoint
    @GetMapping("/{accountId}/collections/maps")
    public ResponseEntity<ResponseDto<List<AdminCollectionMapResponseDto>>> getCollectionMaps(@PathVariable Long accountId) {
        List<AdminCollectionMapResponseDto> items = adminMemberUseCase.getCollectionMapsUseCase(accountId).stream().map(AdminCollectionMapResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.GET_COLLECTION_MAPS.toResponseDto(items));
    }

    @EntryLoggingPoint
    @GetMapping("/{accountId}/collections/mongs")
    public ResponseEntity<ResponseDto<List<AdminCollectionMongResponseDto>>> getCollectionMongs(@PathVariable Long accountId) {
        List<AdminCollectionMongResponseDto> items = adminMemberUseCase.getCollectionMongsUseCase(accountId).stream().map(AdminCollectionMongResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.GET_COLLECTION_MONGS.toResponseDto(items));
    }

    @EntryLoggingPoint
    @PostMapping("/{accountId}/collections/maps")
    public ResponseEntity<ResponseDto<List<AdminCollectionMapResponseDto>>> grantCollectionMap(
            @PathVariable Long accountId,
            @Valid @RequestBody AdminCollectionGrantRequestDto requestDto
    ) {
        List<AdminCollectionMapResponseDto> items = adminMemberUseCase.grantCollectionMapUseCase(accountId, requestDto.getCode()).stream().map(AdminCollectionMapResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.GRANT_COLLECTION_MAP.toResponseDto(items));
    }

    @EntryLoggingPoint
    @PostMapping("/{accountId}/collections/mongs")
    public ResponseEntity<ResponseDto<List<AdminCollectionMongResponseDto>>> grantCollectionMong(
            @PathVariable Long accountId,
            @Valid @RequestBody AdminCollectionGrantRequestDto requestDto
    ) {
        List<AdminCollectionMongResponseDto> items = adminMemberUseCase.grantCollectionMongUseCase(accountId, requestDto.getCode()).stream().map(AdminCollectionMongResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.GRANT_COLLECTION_MONG.toResponseDto(items));
    }
}
