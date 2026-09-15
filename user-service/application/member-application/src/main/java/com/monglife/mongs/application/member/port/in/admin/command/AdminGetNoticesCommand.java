package com.monglife.mongs.application.member.port.in.admin.command;

import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminGetNoticesCommand {

    private final AdminPageRequestVo pageRequest;

    private final String query;

    @Builder
    public AdminGetNoticesCommand(AdminPageRequestVo pageRequest, String query) {
        this.pageRequest = pageRequest;
        this.query = query;
    }
}
