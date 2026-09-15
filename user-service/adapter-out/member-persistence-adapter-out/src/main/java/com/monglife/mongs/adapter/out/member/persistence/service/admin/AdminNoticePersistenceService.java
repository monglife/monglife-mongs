package com.monglife.mongs.adapter.out.member.persistence.service.admin;

import com.monglife.mongs.adapter.out.member.persistence.entity.NoticeEntity;
import com.monglife.mongs.adapter.out.member.persistence.repository.NoticeRepository;
import com.monglife.mongs.adapter.out.member.persistence.repository.admin.AdminNoticeQueryRepository;
import com.monglife.mongs.application.member.port.out.admin.AdminNoticePersistencePort;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.member.model.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminNoticePersistenceService implements AdminNoticePersistencePort {

    private final NoticeRepository noticeRepository;

    private final AdminNoticeQueryRepository adminNoticeQueryRepository;

    @Override
    @Transactional
    public AdminPageVo<Notice> getNoticesPort(AdminPageRequestVo pageRequest, String query) {
        return AdminPageVo.<Notice>builder()
                .page(pageRequest.getPage())
                .size(pageRequest.getSize())
                .total(adminNoticeQueryRepository.count(query))
                .items(adminNoticeQueryRepository.findPage(pageRequest, query).stream().map(NoticeEntity::toDomain).toList())
                .build();
    }

    @Override
    @Transactional
    public Optional<Notice> getNoticePort(Long noticeId) {
        return noticeRepository.findByNoticeId(noticeId).map(NoticeEntity::toDomain);
    }

    @Override
    @Transactional
    public Optional<Notice> createNoticePort(String title, String content, Long writerAccountId, String writerName) {
        NoticeEntity noticeEntity = noticeRepository.save(NoticeEntity.builder()
                .title(title)
                .content(content)
                .writerAccountId(writerAccountId)
                .writerName(writerName)
                .isHided(false)
                .build());
        return Optional.of(noticeEntity.toDomain());
    }

    @Override
    @Transactional
    public Optional<Notice> updateNoticePort(Long noticeId, String title, String content) {
        return noticeRepository.findByNoticeId(noticeId).map(noticeEntity -> {
            noticeEntity.update(title, content);
            return noticeEntity.toDomain();
        });
    }

    @Override
    @Transactional
    public Optional<Notice> updateNoticeHidedPort(Long noticeId, Boolean isHided) {
        return noticeRepository.findByNoticeId(noticeId).map(noticeEntity -> {
            noticeEntity.updateHided(isHided);
            return noticeEntity.toDomain();
        });
    }

    @Override
    @Transactional
    public Optional<Notice> deleteNoticePort(Long noticeId) {
        return noticeRepository.findByNoticeId(noticeId).map(noticeEntity -> {
            noticeRepository.delete(noticeEntity);
            return noticeEntity.toDomain();
        });
    }
}
