package com.monglife.mongs.application.member.port.in.admin.service;

import com.monglife.mongs.application.member.port.in.admin.AdminMemberMasterUseCase;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminExchangeStarPointProductVo;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMapTypeVo;
import com.monglife.mongs.application.member.port.out.admin.AdminMemberMasterReadPort;
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
}
