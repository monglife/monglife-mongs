package com.monglife.mongs.application.member.port.in.admin.service;

import com.monglife.mongs.application.member.port.exception.AlreadyExistsMasterCodeException;
import com.monglife.mongs.application.member.port.exception.NotExistsMasterException;
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

        boolean exists = switch (kind) {
            case MAP_TYPE -> Boolean.TRUE.equals(adminMemberMasterReadPort.isExistsMapTypePort(command.getCode()));
            case EXCHANGE_STAR_POINT_PRODUCT -> Boolean.TRUE.equals(adminMemberMasterReadPort.isExistsExchangeStarPointProductPort(command.getCode()));
        };

        if (exists) throw new AlreadyExistsMasterCodeException();

        AdminAuditLog.write("master created kind={} code={} name={}", kind, command.getCode(), command.getName());

        switch (kind) {
            case MAP_TYPE -> adminMemberMasterReadPort.createMapTypePort(command);
            case EXCHANGE_STAR_POINT_PRODUCT -> adminMemberMasterReadPort.createExchangeStarPointProductPort(command);
        }
    }

    /**
     * 마스터 데이터 삭제. 표의 행만 지우고 공통 코드는 남긴다 —
     * 주문·도감이 코드를 참조하고 있어 함께 지우면 그 행들이 깨진다.
     */
    @Override
    @Transactional
    public void deleteMasterUseCase(Kind kind, String id) {

        boolean deleted = switch (kind) {
            case MAP_TYPE -> Boolean.TRUE.equals(adminMemberMasterReadPort.deleteMapTypePort(Long.valueOf(id)));
            case EXCHANGE_STAR_POINT_PRODUCT -> Boolean.TRUE.equals(adminMemberMasterReadPort.deleteExchangeStarPointProductPort(id));
        };

        if (!deleted) throw new NotExistsMasterException();

        AdminAuditLog.write("master deleted kind={} id={}", kind, id);
    }
}
