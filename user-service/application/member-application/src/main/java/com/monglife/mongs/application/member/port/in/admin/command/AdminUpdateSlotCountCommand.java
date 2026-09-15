package com.monglife.mongs.application.member.port.in.admin.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminUpdateSlotCountCommand {

    private final Long accountId;

    private final Integer slotCount;

    @Builder
    public AdminUpdateSlotCountCommand(Long accountId, Integer slotCount) {
        this.accountId = accountId;
        this.slotCount = slotCount;
    }
}
