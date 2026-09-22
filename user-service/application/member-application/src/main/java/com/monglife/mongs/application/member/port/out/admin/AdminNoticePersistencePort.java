package com.monglife.mongs.application.member.port.out.admin;

import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.member.model.Notice;

import java.util.Optional;

public interface AdminNoticePersistencePort {

    /** 정렬 키: noticeId | createdAt */
    AdminPageVo<Notice> getNoticesPort(AdminPageRequestVo pageRequest, String query);

    /** 숨김 포함 조회 */
    Optional<Notice> getNoticePort(Long noticeId);

    Optional<Notice> createNoticePort(String title, String content, Long writerAccountId, String writerName);

    Optional<Notice> updateNoticePort(Long noticeId, String title, String content);

    Optional<Notice> updateNoticeHidedPort(Long noticeId, Boolean isHided);

    Optional<Notice> deleteNoticePort(Long noticeId);
}
