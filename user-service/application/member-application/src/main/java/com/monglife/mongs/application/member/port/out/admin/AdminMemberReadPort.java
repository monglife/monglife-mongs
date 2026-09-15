package com.monglife.mongs.application.member.port.out.admin;

import com.monglife.mongs.application.member.port.in.admin.vo.AdminMemberVo;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AdminMemberReadPort {

    /** 정렬 키: accountId | starPoint | slotCount | createdAt */
    AdminPageVo<AdminMemberVo> getMembersPort(AdminPageRequestVo pageRequest, Long accountId);

    Optional<AdminMemberVo> getMemberPort(Long accountId);

    Long countMembersPort();

    Long countMembersJoinedSincePort(LocalDateTime since);

    Long sumStarPointPort();
}
