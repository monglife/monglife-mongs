package com.monglife.mongs.adapter.in.admin.user.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monglife.mongs.domain.member.model.Notice;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminNoticeResponseDto(
        Long noticeId,
        String title,
        String content,
        Long writerAccountId,
        String writerName,
        Boolean isHided,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
        LocalDateTime updatedAt
) {
    public static AdminNoticeResponseDto of(Notice notice) {
        return AdminNoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .writerAccountId(notice.getWriterAccountId())
                .writerName(notice.getWriterName())
                .isHided(notice.getIsHided())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
