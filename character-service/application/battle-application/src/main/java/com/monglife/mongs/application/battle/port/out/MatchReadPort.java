package com.monglife.mongs.application.battle.port.out;

import com.monglife.mongs.domain.battle.model.Match;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchReadPort {

    /**
     * 매치 조회
     * @param matchId 매치 ID
     * @return 매치 도메인 객체
     */
    Optional<Match> getMatchPort(Long matchId);

    /**
     * 입장 기한이 지난 ENTERING 매치 ID 목록.
     *
     * <p>행을 잠그지 않는다. 새 인덱스 위에서 잠그면 갭 락이 매치메이커의 INSERT 를 막는다.
     * 실제 전이는 건별로 다시 잠그고 상태를 확인한 뒤에 한다.
     *
     * @param threshold 이 시각보다 오래된 매치
     * @param limit 한 번에 가져올 최대 건수. 배포 직후 쌓여 있던 행이 한꺼번에 쏟아지지 않게 끊는다
     * @return 매치 ID 목록
     */
    List<Long> getExpiredEnteringMatchIdsPort(LocalDateTime threshold, int limit);
}
