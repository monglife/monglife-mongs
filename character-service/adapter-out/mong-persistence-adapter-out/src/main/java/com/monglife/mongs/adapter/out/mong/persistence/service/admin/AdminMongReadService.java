package com.monglife.mongs.adapter.out.mong.persistence.service.admin;

import com.monglife.mongs.adapter.out.mong.persistence.entity.MongEntity;
import com.monglife.mongs.adapter.out.mong.persistence.repository.admin.AdminMongQueryRepository;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongReadPort;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import com.monglife.mongs.domain.mong.model.Mong;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminMongReadService implements AdminMongReadPort {

    private final AdminMongQueryRepository adminMongQueryRepository;

    @Override
    @Transactional
    public AdminPageVo<Mong> getMongsPort(AdminPageRequestVo pageRequest, Long accountId, MongStateCode stateCode, MongStatusCode statusCode, String query) {
        return AdminPageVo.<Mong>builder()
                .page(pageRequest.getPage())
                .size(pageRequest.getSize())
                .total(adminMongQueryRepository.count(accountId, stateCode, statusCode, query))
                .items(adminMongQueryRepository.findPage(pageRequest, accountId, stateCode, statusCode, query).stream().map(MongEntity::toDomain).toList())
                .build();
    }

    @Override
    @Transactional
    public Long countMongsPort() {
        return adminMongQueryRepository.count(null, null, null, null);
    }

    @Override
    @Transactional
    public Long countMongsCreatedSincePort(LocalDateTime since) {
        return adminMongQueryRepository.countCreatedSince(since);
    }

    @Override
    @Transactional
    public Map<MongStateCode, Long> countMongsByStatePort() {
        return adminMongQueryRepository.countByState();
    }

    @Override
    @Transactional
    public Map<MongStatusCode, Long> countMongsByStatusPort() {
        return adminMongQueryRepository.countByStatus();
    }
}
