package com.monglife.mongs.application.member.port.in.admin.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminAdjustStarPointCommand {

    private final Long accountId;

    private final Integer delta;

    @Builder
    public AdminAdjustStarPointCommand(Long accountId, Integer delta) {
        this.accountId = accountId;
        this.delta = delta;
    }
}
