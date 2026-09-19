package com.monglife.mongs.application.mong.port.in.admin.command;

import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminGetMongsCommand {

    private final AdminPageRequestVo pageRequest;

    private final Long accountId;

    private final MongStateCode stateCode;

    private final MongStatusCode statusCode;

    private final String query;

    @Builder
    public AdminGetMongsCommand(AdminPageRequestVo pageRequest, Long accountId, MongStateCode stateCode, MongStatusCode statusCode, String query) {
        this.pageRequest = pageRequest;
        this.accountId = accountId;
        this.stateCode = stateCode;
        this.statusCode = statusCode;
        this.query = query;
    }
}
