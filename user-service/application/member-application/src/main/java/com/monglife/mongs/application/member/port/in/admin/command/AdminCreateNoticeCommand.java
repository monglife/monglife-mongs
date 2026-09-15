package com.monglife.mongs.application.member.port.in.admin.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminCreateNoticeCommand {

    private final String title;

    private final String content;

    private final Long writerAccountId;

    private final String writerName;

    @Builder
    public AdminCreateNoticeCommand(String title, String content, Long writerAccountId, String writerName) {
        this.title = title;
        this.content = content;
        this.writerAccountId = writerAccountId;
        this.writerName = writerName;
    }
}
