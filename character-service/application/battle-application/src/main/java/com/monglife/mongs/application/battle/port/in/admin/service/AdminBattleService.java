package com.monglife.mongs.application.battle.port.in.admin.service;

import com.monglife.mongs.common.admin.log.AdminAuditLog;
import com.monglife.mongs.application.battle.port.exception.NotExistsMatchException;
import com.monglife.mongs.application.battle.port.exception.NotExistsQueuePlayerException;
import com.monglife.mongs.application.battle.port.in.MatchUseCase;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminBattleService implements AdminBattleUseCase {

    private final AdminMatchReadPort adminMatchReadPort;

    private final MatchReadPort matchReadPort;

    private final MatchPersistencePort matchPersistencePort;

    private final MatchPublishPort matchPublishPort;

    private final QueueUseCase queueUseCase;

    private final MatchUseCase matchUseCase;

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

    /**
     * 멈춘 매치 강제 종료.
     *
     * <p>입장 대기 중이던 매치는 아무도 싸우지 않았으므로 참가비를 돌려준다 - 기한 초과
     * 스위퍼와 같은 정산 경로({@code cancelEnteringMatchUseCase})를 탄다. 이미 시작한 매치는
     * 치러진 경기라 예전처럼 보상·정산 없이 END 로만 마감한다.
     *
     * <p>이미 끝난 매치면 {@code AlreadyEndMatchException} 이 올라가 409 가 된다. 관리자 목록은
     * 30초마다 다시 읽고 입장 기한도 30초라, 스위퍼가 방금 CANCELED 로 마감한 매치의 버튼이
     * 화면에 남아 있는 구간이 늘 있다. 그걸 누르면 취소 사유가 END 로 덮이므로 막는다.
     */
    @Override
    public Match terminateMatchUseCase(Long matchId) {

        // 입장 대기 중이면 취소 + 환불. 경합에서 지면(그 사이 시작됐으면) 빈 값이 와서 아래로 떨어진다
        Optional<Match> canceled = matchUseCase.cancelEnteringMatchUseCase(matchId);

        if (canceled.isPresent()) {
            AdminAuditLog.write("match canceled matchId={} (입장 대기 중 - 참가비 환불)", matchId);
            return canceled.get();
        }

        // 진행 중이던 매치는 정산 없이 END 로만 마감한다
        Match match = matchPersistencePort.forceEndMatchPort(matchId)
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
