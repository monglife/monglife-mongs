package com.monglife.mongs.application.mong.port.in.command;

import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class IncreaseMissionProgressCommand {

    private final Long accountId;

    private final MissionActionCode actionCode;

    /** ACCUMULATE 미션이 더할 값. COUNT/DISTINCT 에서는 무시된다 */
    private final Integer amount;

    /** DISTINCT 미션의 집계 대상 코드(음식 코드, 아이템 코드, 몽 코드 ...). 그 외에는 null */
    private final String detailCode;

    @Builder
    public IncreaseMissionProgressCommand(Long accountId, MissionActionCode actionCode, Integer amount, String detailCode) {
        this.accountId = accountId;
        this.actionCode = actionCode;
        this.amount = amount;
        this.detailCode = detailCode;
    }
}
