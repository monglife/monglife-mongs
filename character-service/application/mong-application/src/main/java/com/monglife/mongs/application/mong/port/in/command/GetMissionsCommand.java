package com.monglife.mongs.application.mong.port.in.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GetMissionsCommand {

    private final Long accountId;

    @Builder
    public GetMissionsCommand(Long accountId) {
        this.accountId = accountId;
    }
}
