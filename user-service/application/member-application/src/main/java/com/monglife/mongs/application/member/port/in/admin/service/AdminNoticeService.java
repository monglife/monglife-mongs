package com.monglife.mongs.application.member.port.in.admin.service;

import com.monglife.mongs.application.member.port.exception.InvalidAdminCreateNoticeException;
import com.monglife.mongs.application.member.port.exception.NotExistsNoticeException;
import com.monglife.mongs.application.member.port.in.admin.AdminNoticeUseCase;
import com.monglife.mongs.application.member.port.in.admin.command.AdminCreateNoticeCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminGetNoticesCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminHideNoticeCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminUpdateNoticeCommand;
import com.monglife.mongs.application.member.port.out.admin.AdminNoticePersistencePort;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.member.model.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminNoticeService implements AdminNoticeUseCase {

    private final AdminNoticePersistencePort adminNoticePersistencePort;

    @Override
    @Transactional
    public AdminPageVo<Notice> getNoticesUseCase(AdminGetNoticesCommand command) {
        return adminNoticePersistencePort.getNoticesPort(command.getPageRequest(), command.getQuery());
    }

    @Override
    @Transactional
    public Notice getNoticeUseCase(Long noticeId) {
        return adminNoticePersistencePort.getNoticePort(noticeId)
                .orElseThrow(NotExistsNoticeException::new);
    }

    @Override
    @Transactional
    public Notice createNoticeUseCase(AdminCreateNoticeCommand command) {
        return adminNoticePersistencePort.createNoticePort(command.getTitle(), command.getContent(), command.getWriterAccountId(), command.getWriterName())
                .orElseThrow(InvalidAdminCreateNoticeException::new);
    }

    @Override
    @Transactional
    public Notice updateNoticeUseCase(AdminUpdateNoticeCommand command) {
        return adminNoticePersistencePort.updateNoticePort(command.getNoticeId(), command.getTitle(), command.getContent())
                .orElseThrow(NotExistsNoticeException::new);
    }

    @Override
    @Transactional
    public Notice hideNoticeUseCase(AdminHideNoticeCommand command) {
        return adminNoticePersistencePort.updateNoticeHidedPort(command.getNoticeId(), command.getIsHided())
                .orElseThrow(NotExistsNoticeException::new);
    }

    @Override
    @Transactional
    public Notice deleteNoticeUseCase(Long noticeId) {
        return adminNoticePersistencePort.deleteNoticePort(noticeId)
                .orElseThrow(NotExistsNoticeException::new);
    }
}
