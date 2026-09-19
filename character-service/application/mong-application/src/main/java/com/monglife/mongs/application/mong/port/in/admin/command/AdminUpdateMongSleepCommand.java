package com.monglife.mongs.application.mong.port.in.admin.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminUpdateMongSleepCommand {

    private final Long mongId;

    /** true 면 수면, false 면 기상 */
    private final Boolean isSleep;

    private final String reason;

    @Builder
    public AdminUpdateMongSleepCommand(Long mongId, Boolean isSleep, String reason) {
        this.mongId = mongId;
        this.isSleep = isSleep;
        this.reason = reason;
    }
}
