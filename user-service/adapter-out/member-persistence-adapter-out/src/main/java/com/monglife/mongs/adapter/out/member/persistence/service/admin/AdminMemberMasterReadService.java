package com.monglife.mongs.adapter.out.member.persistence.service.admin;

import com.monglife.module.common.jpa.entity.ComnCodeEntity;
import com.monglife.module.common.jpa.entity.GroupCodeEntity;
import com.monglife.mongs.adapter.out.member.persistence.entity.ExchangeStarPointProductEntity;
import com.monglife.mongs.adapter.out.member.persistence.entity.MapTypeEntity;
import com.monglife.mongs.adapter.out.member.persistence.repository.ComnCodeRepository;
import com.monglife.mongs.adapter.out.member.persistence.repository.ExchangeStarPointProductRepository;
import com.monglife.mongs.adapter.out.member.persistence.repository.GroupCodeRepository;
import com.monglife.mongs.adapter.out.member.persistence.repository.MapTypeRepository;
import com.monglife.mongs.application.member.port.in.admin.command.AdminCreateMasterCommand;
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

    /** 종류별 공통 코드 그룹. monglife_group_code 의 시드와 같아야 한다 */
    private static final String GROUP_MAP = "MP";
    private static final String GROUP_PRODUCT = "PRDT";

    private final MapTypeRepository mapTypeRepository;

    private final ExchangeStarPointProductRepository exchangeStarPointProductRepository;

    private final ComnCodeRepository comnCodeRepository;

    private final GroupCodeRepository groupCodeRepository;

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


    @Override
    @Transactional
    public Boolean isExistsMapTypePort(String code) {
        return mapTypeRepository.findByComnCode(code).isPresent();
    }

    @Override
    @Transactional
    public Boolean isExistsExchangeStarPointProductPort(String productId) {
        return exchangeStarPointProductRepository.findByProductId(productId).isPresent();
    }

    @Override
    @Transactional
    public Boolean deleteMapTypePort(Long id) {
        return mapTypeRepository.findById(id)
                .map(entity -> {
                    mapTypeRepository.delete(entity);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public Boolean deleteExchangeStarPointProductPort(String productId) {
        return exchangeStarPointProductRepository.findByProductId(productId)
                .map(entity -> {
                    exchangeStarPointProductRepository.delete(entity);
                    return true;
                })
                .orElse(false);
    }
    @Override
    @Transactional
    public void createMapTypePort(AdminCreateMasterCommand command) {
        ComnCodeEntity comn = comnCode(command, GROUP_MAP);
        mapTypeRepository.save(MapTypeEntity.builder()
                .comn(comn)
                .words(command.getWords())
                .build());
    }

    /**
     * 환전 상품은 상품 표(productId)와 공통 코드를 둘 다 만든다.
     * 주문(mongs_order)이 product_code 로 공통 코드를 참조하기 때문이다.
     */
    @Override
    @Transactional
    public void createExchangeStarPointProductPort(AdminCreateMasterCommand command) {
        comnCode(command, GROUP_PRODUCT);
        exchangeStarPointProductRepository.save(ExchangeStarPointProductEntity.builder()
                .productId(command.getCode())
                .productName(command.getName())
                .starPoint(command.getStarPoint())
                .build());
    }

    /**
     * 공통 코드를 만들어 붙인다. 그룹 코드가 없으면 함께 만든다 —
     * 시드가 들어가지 않은 환경(빈 DB)에서도 등록이 되게 한다.
     */
    private ComnCodeEntity comnCode(AdminCreateMasterCommand command, String groupCode) {
        return comnCodeRepository.findById(command.getCode()).orElseGet(() -> {
            GroupCodeEntity group = groupCodeRepository.findById(groupCode)
                    .orElseGet(() -> groupCodeRepository.save(GroupCodeEntity.builder().code(groupCode).name(groupCode).build()));
            return comnCodeRepository.save(ComnCodeEntity.builder()
                    .code(command.getCode())
                    .name(command.getName())
                    .group(group)
                    .build());
        });
    }
}
