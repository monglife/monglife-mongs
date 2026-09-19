package com.monglife.mongs.application.member.port.in.admin;

import com.monglife.mongs.application.member.port.in.admin.command.AdminCreateNoticeCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminGetNoticesCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminHideNoticeCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminUpdateNoticeCommand;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.member.model.Notice;

public interface AdminNoticeUseCase {

    /** 공지 사항 목록 조회 (숨김 포함) */
    AdminPageVo<Notice> getNoticesUseCase(AdminGetNoticesCommand command);

    /** 공지 사항 조회 (숨김 포함) */
    Notice getNoticeUseCase(Long noticeId);

    Notice createNoticeUseCase(AdminCreateNoticeCommand command);

    Notice updateNoticeUseCase(AdminUpdateNoticeCommand command);

    Notice hideNoticeUseCase(AdminHideNoticeCommand command);

    Notice deleteNoticeUseCase(Long noticeId);
}
