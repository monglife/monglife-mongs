package com.monglife.mongs.application.mong.port.in.command;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ClaimMissionRewardCommand {

    private final Long accountId;

    private final Long accountMissionId;

    /** 리워드를 받을 몽. 경험치·페이 포인트·인벤토리가 몽 소유라 대상이 필요하다 */
    private final Long mongId;

    @Builder
    public ClaimMissionRewardCommand(Long accountId, Long accountMissionId, Long mongId) {
        this.accountId = accountId;
        this.accountMissionId = accountMissionId;
        this.mongId = mongId;
    }
}
