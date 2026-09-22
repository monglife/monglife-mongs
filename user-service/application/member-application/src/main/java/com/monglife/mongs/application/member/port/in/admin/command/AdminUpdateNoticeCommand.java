package com.monglife.mongs.application.member.port.in.admin.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminUpdateNoticeCommand {

    private final Long noticeId;

    private final String title;

    private final String content;

    @Builder
    public AdminUpdateNoticeCommand(Long noticeId, String title, String content) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
    }
}
