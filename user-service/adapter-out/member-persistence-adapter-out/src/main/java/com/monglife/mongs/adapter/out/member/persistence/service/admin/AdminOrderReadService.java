package com.monglife.mongs.adapter.out.member.persistence.service.admin;

import com.monglife.mongs.adapter.out.member.persistence.entity.OrderEntity;
import com.monglife.mongs.adapter.out.member.persistence.repository.admin.AdminOrderQueryRepository;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminOrderVo;
import com.monglife.mongs.application.member.port.out.admin.AdminOrderReadPort;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminOrderReadService implements AdminOrderReadPort {

    private final AdminOrderQueryRepository adminOrderQueryRepository;

    @Override
    @Transactional
    public AdminPageVo<AdminOrderVo> getOrdersPort(AdminPageRequestVo pageRequest, Long accountId, String productId) {
        return AdminPageVo.<AdminOrderVo>builder()
                .page(pageRequest.getPage())
                .size(pageRequest.getSize())
                .total(adminOrderQueryRepository.count(accountId, productId))
                .items(adminOrderQueryRepository.findPage(pageRequest, accountId, productId).stream().map(this::toVo).toList())
                .build();
    }

    @Override
    @Transactional
    public Optional<AdminOrderVo> getOrderPort(Long orderId) {
        return adminOrderQueryRepository.findByOrderId(orderId).map(this::toVo);
    }

    @Override
    @Transactional
    public Long countOrdersPort() {
        return adminOrderQueryRepository.countSince(null);
    }

    @Override
    @Transactional
    public Long countOrdersSincePort(LocalDateTime since) {
        return adminOrderQueryRepository.countSince(since);
    }

    @Override
    @Transactional
    public Double sumOrderPriceSincePort(LocalDateTime since) {
        return adminOrderQueryRepository.sumPriceSince(since);
    }

    private AdminOrderVo toVo(OrderEntity orderEntity) {
        return AdminOrderVo.builder()
                .orderId(orderEntity.getOrderId())
                .accountId(orderEntity.getAccountId())
                .productId(orderEntity.getProductType() == null ? null : orderEntity.getProductType().getCode())
                .productName(orderEntity.getProductType() == null ? null : orderEntity.getProductType().getName())
                .price(orderEntity.getPrice())
                .socialOrderId(orderEntity.getSocialOrderId())
                .createdAt(orderEntity.getCreatedAt())
                .build();
    }
}
