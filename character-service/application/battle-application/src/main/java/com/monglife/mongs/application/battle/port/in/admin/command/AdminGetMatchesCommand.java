package com.monglife.mongs.application.battle.port.in.admin.command;

import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.domain.battle.enums.MatchStateCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminGetMatchesCommand {

    private final AdminPageRequestVo pageRequest;

    private final MatchStateCode stateCode;

    private final Long accountId;

    @Builder
    public AdminGetMatchesCommand(AdminPageRequestVo pageRequest, MatchStateCode stateCode, Long accountId) {
        this.pageRequest = pageRequest;
        this.stateCode = stateCode;
        this.accountId = accountId;
    }
}
