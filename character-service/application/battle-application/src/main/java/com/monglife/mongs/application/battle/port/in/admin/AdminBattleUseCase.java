package com.monglife.mongs.application.battle.port.in.admin;

import com.monglife.mongs.application.battle.port.in.admin.command.AdminGetMatchesCommand;
import com.monglife.mongs.application.battle.port.in.admin.vo.AdminBattleStatsVo;
import com.monglife.mongs.application.battle.port.in.admin.vo.AdminMatchSummaryVo;
import com.monglife.mongs.application.battle.port.in.admin.vo.AdminQueuePlayerVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.battle.model.Match;
import com.monglife.mongs.domain.battle.model.QueuePlayer;

import java.util.List;

public interface AdminBattleUseCase {

    List<AdminQueuePlayerVo> getQueuePlayersUseCase();

    /** 대기열 강제 이탈. 기존 이탈 유스케이스(배팅 환불 포함)를 그대로 태운다 */
    QueuePlayer deleteQueuePlayerUseCase(Long mongId);

    AdminPageVo<AdminMatchSummaryVo> getMatchesUseCase(AdminGetMatchesCommand command);

    Match getMatchUseCase(Long matchId);

    /** 멈춘 매치 강제 종료. 보상 없이 END 로 마감하고 앱에 종료를 알린다 */
    Match terminateMatchUseCase(Long matchId);

    AdminBattleStatsVo getStatsUseCase();
}
