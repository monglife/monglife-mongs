package com.monglife.mongs.application.member.port.in.admin.command;

import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminGetMembersCommand {

    private final AdminPageRequestVo pageRequest;

    private final Long accountId;

    @Builder
    public AdminGetMembersCommand(AdminPageRequestVo pageRequest, Long accountId) {
        this.pageRequest = pageRequest;
        this.accountId = accountId;
    }
}
