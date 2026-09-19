package com.monglife.mongs.application.member.port.in.admin.command;

import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AdminGetOrdersCommand {

    private final AdminPageRequestVo pageRequest;

    private final Long accountId;

    private final String productId;

    @Builder
    public AdminGetOrdersCommand(AdminPageRequestVo pageRequest, Long accountId, String productId) {
        this.pageRequest = pageRequest;
        this.accountId = accountId;
        this.productId = productId;
    }
}
