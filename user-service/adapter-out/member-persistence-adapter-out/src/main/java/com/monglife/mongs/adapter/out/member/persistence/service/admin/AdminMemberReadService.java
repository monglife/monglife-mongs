package com.monglife.mongs.adapter.out.member.persistence.service.admin;

import com.monglife.mongs.adapter.out.member.persistence.entity.MemberEntity;
import com.monglife.mongs.adapter.out.member.persistence.repository.MemberRepository;
import com.monglife.mongs.adapter.out.member.persistence.repository.admin.AdminMemberQueryRepository;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMemberVo;
import com.monglife.mongs.application.member.port.out.admin.AdminMemberReadPort;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminMemberReadService implements AdminMemberReadPort {

    private final MemberRepository memberRepository;

    private final AdminMemberQueryRepository adminMemberQueryRepository;

    @Override
    @Transactional
    public AdminPageVo<AdminMemberVo> getMembersPort(AdminPageRequestVo pageRequest, Long accountId) {
        return AdminPageVo.<AdminMemberVo>builder()
                .page(pageRequest.getPage())
                .size(pageRequest.getSize())
                .total(adminMemberQueryRepository.count(accountId))
                .items(adminMemberQueryRepository.findPage(pageRequest, accountId).stream().map(this::toVo).toList())
                .build();
    }

    @Override
    @Transactional
    public Optional<AdminMemberVo> getMemberPort(Long accountId) {
        return memberRepository.findByAccountId(accountId).map(this::toVo);
    }

    @Override
    @Transactional
    public Long countMembersPort() {
        return adminMemberQueryRepository.count(null);
    }

    @Override
    @Transactional
    public Long countMembersJoinedSincePort(LocalDateTime since) {
        return adminMemberQueryRepository.countJoinedSince(since);
    }

    @Override
    @Transactional
    public Long sumStarPointPort() {
        return adminMemberQueryRepository.sumStarPoint();
    }

    private AdminMemberVo toVo(MemberEntity memberEntity) {
        return AdminMemberVo.builder()
                .accountId(memberEntity.getAccountId())
                .slotCount(memberEntity.getSlotCount())
                .starPoint(memberEntity.getStarPoint())
                .createdAt(memberEntity.getCreatedAt())
                .updatedAt(memberEntity.getUpdatedAt())
                .build();
    }
}
