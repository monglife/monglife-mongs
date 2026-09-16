package com.monglife.mongs.application.battle.port.in.admin.service;

import com.monglife.mongs.common.admin.log.AdminAuditLog;
import com.monglife.mongs.application.battle.port.exception.NotExistsMatchException;
import com.monglife.mongs.application.battle.port.exception.NotExistsQueuePlayerException;
import com.monglife.mongs.application.battle.port.in.QueueUseCase;
import com.monglife.mongs.application.battle.port.in.admin.AdminBattleUseCase;
import com.monglife.mongs.application.battle.port.in.admin.command.AdminGetMatchesCommand;
import com.monglife.mongs.application.battle.port.in.admin.vo.AdminBattleStatsVo;
import com.monglife.mongs.application.battle.port.in.admin.vo.AdminMatchSummaryVo;
import com.monglife.mongs.application.battle.port.in.admin.vo.AdminQueuePlayerVo;
import com.monglife.mongs.application.battle.port.in.command.DeleteQueuePlayerCommand;
import com.monglife.mongs.application.battle.port.out.MatchPersistencePort;
import com.monglife.mongs.application.battle.port.out.MatchPublishPort;
import com.monglife.mongs.application.battle.port.out.MatchReadPort;
import com.monglife.mongs.application.battle.port.out.admin.AdminMatchReadPort;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.battle.enums.MatchStateCode;
import com.monglife.mongs.domain.battle.model.Match;
import com.monglife.mongs.domain.battle.model.QueuePlayer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminBattleService implements AdminBattleUseCase {

    private final AdminMatchReadPort adminMatchReadPort;

    private final MatchReadPort matchReadPort;

    private final MatchPersistencePort matchPersistencePort;

    private final MatchPublishPort matchPublishPort;

    private final QueueUseCase queueUseCase;

    @Override
    public List<AdminQueuePlayerVo> getQueuePlayersUseCase() {
        return adminMatchReadPort.getQueuePlayersPort();
    }

    @Override
    @Transactional
    public QueuePlayer deleteQueuePlayerUseCase(Long mongId) {

        AdminQueuePlayerVo queuePlayer = adminMatchReadPort.getQueuePlayersPort().stream()
                .filter(vo -> vo.getMongId().equals(mongId))
                .findFirst()
                .orElseThrow(NotExistsQueuePlayerException::new);

        AdminAuditLog.write("queue player removed mongId={} accountId={} deviceId={}", mongId, queuePlayer.getAccountId(), queuePlayer.getDeviceId());

        return queueUseCase.deleteQueuePlayerUseCase(DeleteQueuePlayerCommand.builder()
                .mongId(queuePlayer.getMongId())
                .accountId(queuePlayer.getAccountId())
                .deviceId(queuePlayer.getDeviceId())
                .build());
    }

    @Override
    @Transactional
    public AdminPageVo<AdminMatchSummaryVo> getMatchesUseCase(AdminGetMatchesCommand command) {
        return adminMatchReadPort.getMatchesPort(command.getPageRequest(), command.getStateCode(), command.getAccountId());
    }

    @Override
    @Transactional
    public Match getMatchUseCase(Long matchId) {
        return matchReadPort.getMatchPort(matchId)
                .orElseThrow(NotExistsMatchException::new);
    }

    @Override
    @Transactional
    public Match terminateMatchUseCase(Long matchId) {

        Match match = matchPersistencePort.getMatchPort(matchId)
                .orElseThrow(NotExistsMatchException::new);

        match.adminEnd();

        match = matchPersistencePort.saveMatchPort(match)
                .orElseThrow(NotExistsMatchException::new);

        AdminAuditLog.write("match terminated matchId={}", matchId);

        // 매치 강제 중단 비동기 응답
        matchPublishPort.publishMatchEndPort(match);

        return match;
    }

    @Override
    @Transactional
    public AdminBattleStatsVo getStatsUseCase() {

        LocalDateTime today = LocalDate.now().atStartOfDay();

        return AdminBattleStatsVo.builder()
                .queueSize((long) adminMatchReadPort.getQueuePlayersPort().size())
                .totalMatches(adminMatchReadPort.countMatchesPort(null, null))
                .todayMatches(adminMatchReadPort.countMatchesPort(null, today))
                .todayBotMatches(adminMatchReadPort.countBotMatchesSincePort(today))
                .processingMatches(adminMatchReadPort.countMatchesPort(MatchStateCode.PROCESS, null))
                .build();
    }
}
