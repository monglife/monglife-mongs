package com.monglife.mongs.application.member.port.in.admin.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminAdjustStarPointCommand {

    private final Long accountId;

    private final Integer delta;

    private final String reason;

    @Builder
    public AdminAdjustStarPointCommand(Long accountId, Integer delta, String reason) {
        this.accountId = accountId;
        this.delta = delta;
        this.reason = reason;
    }
}
