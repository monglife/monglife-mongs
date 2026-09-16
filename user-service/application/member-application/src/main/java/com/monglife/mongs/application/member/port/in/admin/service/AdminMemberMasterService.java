package com.monglife.mongs.application.member.port.in.admin.service;

import com.monglife.mongs.application.member.port.exception.AlreadyExistsMasterCodeException;
import com.monglife.mongs.application.member.port.in.admin.AdminMemberMasterUseCase;
import com.monglife.mongs.application.member.port.in.admin.command.AdminCreateMasterCommand;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminExchangeStarPointProductVo;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMapTypeVo;
import com.monglife.mongs.application.member.port.out.admin.AdminMemberMasterReadPort;
import com.monglife.mongs.common.admin.log.AdminAuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMemberMasterService implements AdminMemberMasterUseCase {

    private final AdminMemberMasterReadPort adminMemberMasterReadPort;

    @Override
    @Transactional
    public List<AdminMapTypeVo> getMapTypesUseCase() {
        return adminMemberMasterReadPort.getMapTypesPort();
    }

    @Override
    @Transactional
    public List<AdminExchangeStarPointProductVo> getExchangeStarPointProductsUseCase() {
        return adminMemberMasterReadPort.getExchangeStarPointProductsPort();
    }

    @Override
    @Transactional
    public void createMasterUseCase(Kind kind, AdminCreateMasterCommand command) {

        if (Boolean.TRUE.equals(adminMemberMasterReadPort.isExistsComnCodePort(command.getCode()))) {
            throw new AlreadyExistsMasterCodeException();
        }

        AdminAuditLog.write("master created kind={} code={} name={}", kind, command.getCode(), command.getName());

        switch (kind) {
            case MAP_TYPE -> adminMemberMasterReadPort.createMapTypePort(command);
            case EXCHANGE_STAR_POINT_PRODUCT -> adminMemberMasterReadPort.createExchangeStarPointProductPort(command);
        }
    }
}
