package com.monglife.mongs.adapter.in.admin.user.web.controller;

import com.monglife.core.dto.response.PageResponseDto;
import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.module.common.security.principal.Passport;
import com.monglife.mongs.adapter.in.admin.user.web.dto.request.AdminNoticeHideRequestDto;
import com.monglife.mongs.adapter.in.admin.user.web.dto.request.AdminNoticeRequestDto;
import com.monglife.mongs.adapter.in.admin.user.web.dto.response.AdminNoticeResponseDto;
import com.monglife.mongs.adapter.in.admin.user.web.enums.AdapterInAdminUserWebResponse;
import com.monglife.mongs.adapter.in.admin.user.web.util.AdminPage;
import com.monglife.mongs.adapter.in.admin.user.web.util.PageQuery;
import com.monglife.mongs.application.member.port.in.admin.AdminNoticeUseCase;
import com.monglife.mongs.application.member.port.in.admin.command.AdminCreateNoticeCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminGetNoticesCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminHideNoticeCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminUpdateNoticeCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private static final Set<String> SORT_KEYS = Set.of("noticeId", "createdAt");

    private final AdminNoticeUseCase adminNoticeUseCase;

    @EntryLoggingPoint
    @GetMapping
    public ResponseEntity<PageResponseDto<List<AdminNoticeResponseDto>>> getNotices(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort
    ) {
        AdminGetNoticesCommand command = AdminGetNoticesCommand.builder()
                .pageRequest(PageQuery.of(page, size, sort, SORT_KEYS, "noticeId", true))
                .query(PageQuery.blankToNull(query))
                .build();

        return AdminPage.toResponse(AdapterInAdminUserWebResponse.GET_NOTICES, adminNoticeUseCase.getNoticesUseCase(command), AdminNoticeResponseDto::of);
    }

    @EntryLoggingPoint
    @GetMapping("/{noticeId}")
    public ResponseEntity<ResponseDto<AdminNoticeResponseDto>> getNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.GET_NOTICE.toResponseDto(AdminNoticeResponseDto.of(adminNoticeUseCase.getNoticeUseCase(noticeId))));
    }

    @EntryLoggingPoint
    @PostMapping
    public ResponseEntity<ResponseDto<AdminNoticeResponseDto>> createNotice(
            @AuthenticationPrincipal Passport passport,
            @Valid @RequestBody AdminNoticeRequestDto requestDto
    ) {
        AdminCreateNoticeCommand command = AdminCreateNoticeCommand.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .writerAccountId(passport.getAccountId())
                .writerName(passport.getName() == null || passport.getName().isBlank() ? "관리자" : passport.getName())
                .build();

        return ResponseEntity.ok(AdapterInAdminUserWebResponse.CREATE_NOTICE.toResponseDto(AdminNoticeResponseDto.of(adminNoticeUseCase.createNoticeUseCase(command))));
    }

    @EntryLoggingPoint
    @PutMapping("/{noticeId}")
    public ResponseEntity<ResponseDto<AdminNoticeResponseDto>> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody AdminNoticeRequestDto requestDto
    ) {
        AdminUpdateNoticeCommand command = AdminUpdateNoticeCommand.builder()
                .noticeId(noticeId)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .build();

        return ResponseEntity.ok(AdapterInAdminUserWebResponse.UPDATE_NOTICE.toResponseDto(AdminNoticeResponseDto.of(adminNoticeUseCase.updateNoticeUseCase(command))));
    }

    @EntryLoggingPoint
    @PatchMapping("/{noticeId}/hide")
    public ResponseEntity<ResponseDto<AdminNoticeResponseDto>> hideNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody AdminNoticeHideRequestDto requestDto
    ) {
        AdminHideNoticeCommand command = AdminHideNoticeCommand.builder()
                .noticeId(noticeId)
                .isHided(requestDto.getIsHided())
                .build();

        return ResponseEntity.ok(AdapterInAdminUserWebResponse.HIDE_NOTICE.toResponseDto(AdminNoticeResponseDto.of(adminNoticeUseCase.hideNoticeUseCase(command))));
    }

    @EntryLoggingPoint
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ResponseDto<AdminNoticeResponseDto>> deleteNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.DELETE_NOTICE.toResponseDto(AdminNoticeResponseDto.of(adminNoticeUseCase.deleteNoticeUseCase(noticeId))));
    }
}
