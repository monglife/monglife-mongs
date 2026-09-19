package com.monglife.mongs.application.mong.port.in.admin.command;

import com.monglife.mongs.domain.mong.enums.MongStateCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminUpdateMongStateCommand {

    private final Long mongId;

    private final MongStateCode stateCode;

    @Builder
    public AdminUpdateMongStateCommand(Long mongId, MongStateCode stateCode) {
        this.mongId = mongId;
        this.stateCode = stateCode;
    }
}
