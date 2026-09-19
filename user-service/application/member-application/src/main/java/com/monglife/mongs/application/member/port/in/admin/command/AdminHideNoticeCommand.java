package com.monglife.mongs.application.member.port.in.admin.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminHideNoticeCommand {

    private final Long noticeId;

    private final Boolean isHided;

    @Builder
    public AdminHideNoticeCommand(Long noticeId, Boolean isHided) {
        this.noticeId = noticeId;
        this.isHided = isHided;
    }
}
