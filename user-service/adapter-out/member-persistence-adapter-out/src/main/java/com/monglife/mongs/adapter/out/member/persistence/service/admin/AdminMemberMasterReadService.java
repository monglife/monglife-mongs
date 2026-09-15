package com.monglife.mongs.adapter.out.member.persistence.service.admin;

import com.monglife.mongs.adapter.out.member.persistence.repository.ExchangeStarPointProductRepository;
import com.monglife.mongs.adapter.out.member.persistence.repository.MapTypeRepository;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminExchangeStarPointProductVo;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMapTypeVo;
import com.monglife.mongs.application.member.port.out.admin.AdminMemberMasterReadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMemberMasterReadService implements AdminMemberMasterReadPort {

    private final MapTypeRepository mapTypeRepository;

    private final ExchangeStarPointProductRepository exchangeStarPointProductRepository;

    @Override
    @Transactional
    public List<AdminMapTypeVo> getMapTypesPort() {
        return mapTypeRepository.findAll().stream()
                .map(mapTypeEntity -> AdminMapTypeVo.builder()
                        .mapTypeId(mapTypeEntity.getMapTypeId())
                        .mapCode(mapTypeEntity.getComn().getCode())
                        .mapName(mapTypeEntity.getComn().getName())
                        .words(mapTypeEntity.getWords())
                        .build())
                .sorted(Comparator.comparing(AdminMapTypeVo::getMapCode))
                .toList();
    }

    @Override
    @Transactional
    public List<AdminExchangeStarPointProductVo> getExchangeStarPointProductsPort() {
        return exchangeStarPointProductRepository.findAll().stream()
                .map(productEntity -> AdminExchangeStarPointProductVo.builder()
                        .productId(productEntity.getProductId())
                        .productName(productEntity.getProductName())
                        .starPoint(productEntity.getStarPoint())
                        .build())
                .sorted(Comparator.comparing(AdminExchangeStarPointProductVo::getProductId))
                .toList();
    }
}
