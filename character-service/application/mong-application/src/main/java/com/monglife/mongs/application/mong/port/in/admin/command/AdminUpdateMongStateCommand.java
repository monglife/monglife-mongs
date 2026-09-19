package com.monglife.mongs.application.mong.port.in.admin.command;

import com.monglife.mongs.domain.mong.enums.MongStateCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminUpdateMongStateCommand {

    private final Long mongId;

    private final MongStateCode stateCode;

    private final String reason;

    @Builder
    public AdminUpdateMongStateCommand(Long mongId, MongStateCode stateCode, String reason) {
        this.mongId = mongId;
        this.stateCode = stateCode;
        this.reason = reason;
    }
}
