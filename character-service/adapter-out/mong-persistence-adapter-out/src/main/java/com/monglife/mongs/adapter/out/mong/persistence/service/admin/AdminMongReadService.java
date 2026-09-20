package com.monglife.mongs.adapter.out.mong.persistence.service.admin;

import com.monglife.mongs.adapter.out.mong.persistence.entity.InventoryEntity;
import com.monglife.mongs.adapter.out.mong.persistence.entity.MongEntity;
import com.monglife.mongs.adapter.out.mong.persistence.repository.InventoryRepository;
import com.monglife.mongs.adapter.out.mong.persistence.repository.admin.AdminMongQueryRepository;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongReadPort;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import com.monglife.mongs.domain.mong.model.Inventory;
import com.monglife.mongs.domain.mong.model.Mong;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminMongReadService implements AdminMongReadPort {

    private final AdminMongQueryRepository adminMongQueryRepository;

    private final InventoryRepository inventoryRepository;

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

    /**
     * 인벤토리 목록. Spring Data 가 count 쿼리를 이미 돌려 {@code getTotalElements()} 를 채워 두므로
     * 총 건수를 쓰는 데 추가 비용이 없다. 앱 경로는 이 값을 버리고 페이지 수만 쓴다.
     */
    @Override
    @Transactional
    public AdminPageVo<Inventory> getInventoriesPort(AdminPageRequestVo pageRequest, Long mongId) {

        Page<InventoryEntity> page = inventoryRepository.findByMongId(
                PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), Sort.by(Sort.Direction.ASC, "inventoryId")),
                mongId);

        return AdminPageVo.<Inventory>builder()
                .page(pageRequest.getPage())
                .size(pageRequest.getSize())
                .total(page.getTotalElements())
                .items(page.getContent().stream().map(InventoryEntity::toDomain).toList())
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
