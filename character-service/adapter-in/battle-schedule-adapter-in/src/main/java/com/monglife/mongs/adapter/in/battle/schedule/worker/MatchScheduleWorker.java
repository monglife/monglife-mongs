package com.monglife.mongs.adapter.in.battle.schedule.worker;

import com.monglife.mongs.application.battle.port.in.MatchUseCase;
import com.monglife.mongs.application.battle.port.out.MatchReadPort;
import com.monglife.mongs.domain.battle.model.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 입장 기한을 넘긴 매치를 거둬들인다.
 *
 * <p>매치는 클라이언트가 입장 이벤트를 보내야만 {@code ENTERING} 을 벗어난다. 그 이벤트가
 * 오지 않으면 아무것도 그 매치를 건드리지 않고, 이미 빠져나간 참가비 50 페이 포인트도
 * 돌아오지 않는다 - 앱에는 취소 수단조차 없다. 서버가 거둬야 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchScheduleWorker {

    /**
     * 한 틱에 처리할 최대 건수.
     *
     * <p>끊어 두는 이유는 첫 틱 때문이다. 이 기능이 없던 동안 쌓인 ENTERING 행이 한꺼번에
     * 쏟아지면 한 틱이 길어지고 그만큼 매칭 스케줄과 실행기를 나눠 쓰는 시간이 는다.
     * 남은 건 다음 틱이 가져간다.
     */
    private static final int BATCH_SIZE = 100;

    private final MatchReadPort matchReadPort;

    private final MatchUseCase matchUseCase;

    /**
     * 입장 기한 초과 매치 취소 스케줄.
     *
     * <p>후보 조회는 잠그지 않는다. 취소는 건별로 다시 잠그고 상태를 확인한 뒤에 하므로,
     * 고르고 나서 입장이 도착한 매치는 그 자리에서 걸러진다.
     */
    public void doExpireEnteringMatches() {

        LocalDateTime threshold = LocalDateTime.now().minusSeconds(Match.getEnterExpiredSeconds());

        List<Long> matchIds = matchReadPort.getExpiredEnteringMatchIdsPort(threshold, BATCH_SIZE);

        for (Long matchId : matchIds) {
            try {
                matchUseCase.cancelEnteringMatchUseCase(matchId)
                        .ifPresent(match -> log.info("입장 기한 초과 - 매치 취소 matchId={}", match.getMatchId()));
            } catch (Exception exception) {
                // 한 건이 실패해도 나머지는 거둔다. 다음 틱에 다시 후보로 잡힌다.
                log.error("입장 기한 초과 매치 취소 실패 matchId={}", matchId, exception);
            }
        }
    }
}
