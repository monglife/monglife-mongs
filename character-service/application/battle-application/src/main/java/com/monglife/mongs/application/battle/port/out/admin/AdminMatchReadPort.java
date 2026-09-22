package com.monglife.mongs.application.battle.port.out.admin;

import com.monglife.mongs.application.battle.port.in.admin.vo.AdminMatchSummaryVo;
import com.monglife.mongs.application.battle.port.in.admin.vo.AdminQueuePlayerVo;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.battle.enums.MatchStateCode;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminMatchReadPort {

    List<AdminQueuePlayerVo> getQueuePlayersPort();

    /** 정렬 키: matchId | createdAt */
    AdminPageVo<AdminMatchSummaryVo> getMatchesPort(AdminPageRequestVo pageRequest, MatchStateCode stateCode, Long accountId);

    Long countMatchesPort(MatchStateCode stateCode, LocalDateTime since);

    /** 봇이 한 명이라도 낀 매치 수 */
    Long countBotMatchesSincePort(LocalDateTime since);
}
