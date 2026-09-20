package com.monglife.mongs.application.battle.port.out;

import com.monglife.mongs.application.battle.port.out.vo.CreateMatchVo;
import com.monglife.mongs.application.battle.port.out.vo.CreateQueuePlayerVo;
import com.monglife.mongs.domain.battle.model.Match;
import com.monglife.mongs.domain.battle.model.QueuePlayer;

import java.util.List;
import java.util.Optional;

public interface MatchPersistencePort {

    /**
     * 매치 대기열 조회
     * @param mongId 몽 ID
     * @param accountId 계정 ID
     * @param deviceId 기기 ID
     * @return 매치 대기열 도메인 객체
     */
    Optional<QueuePlayer> getQueuePlayerPort(Long mongId, Long accountId, String deviceId);

    /**
     * 매치 대기열 목록 조회
     * @param matchPlayerCount 매치 플레이어 수
     * @param expiredSeconds 봇 매칭 대기 시간
     * @return 매치 대기열 도메인 객체 목록
     */
    List<QueuePlayer> getQueuePlayersPort(Integer matchPlayerCount, Long expiredSeconds);

    /**
     * 매치 대기열 등록
     * @param createQueuePlayerVo 매치 대기열 등록 Vo
     * @return 매치 대기열 도메인 객체
     */
    Optional<QueuePlayer> createQueuePlayerPort(CreateQueuePlayerVo createQueuePlayerVo);

    /**
     * 매치 대기열 삭제
     * @param queuePlayer 삭제할 매치 대기열 도메인 객체
     * @return 매치 대기열 도메인 객체
     */
    Optional<QueuePlayer> deleteQueuePlayerPort(QueuePlayer queuePlayer);

    /**
     * 매치 조회
     * @param matchId 매치 ID
     * @return 매치 도메인 객체
     */
    Optional<Match> getMatchPort(Long matchId);

    /**
     * 매치 등록
     * @param createMatchVo 매치 등록 Vo
     * @return 매치 도메인 객체
     */
    Optional<Match> createMatchPort(CreateMatchVo createMatchVo);

    /**
     * 매치 동기화
     * @param match 매치 도메인 객체
     * @return 매치 도메인 객체
     */
    Optional<Match> saveMatchPort(Match match);

    /**
     * 입장 기한이 지난 매치를 취소 상태로 바꾼다. 조회·검사·저장을 한 트랜잭션 안에서 한다.
     *
     * <p>행을 잠근 뒤 상태를 <b>다시</b> 확인한다. 후보를 고르는 조회는 잠그지 않기 때문에
     * (잠그면 새 매치 INSERT 를 막는다) 그 사이 마지막 플레이어가 입장했을 수 있다.
     * 그 경합에서 지면 빈 값을 돌려주고 호출자는 그냥 넘어간다.
     *
     * @param matchId 매치 ID
     * @return 취소된 매치. 이미 시작했거나 없으면 빈 값
     */
    Optional<Match> cancelEnteringMatchPort(Long matchId);

    /**
     * 진행 중이던 매치를 정산 없이 END 로 마감한다 (관리자 강제 종료).
     *
     * <p>조회·전이·저장을 한 트랜잭션에서 한다. 입장 대기 중인 매치에는 쓰지 않는다 -
     * 그쪽은 참가비를 돌려줘야 해서 {@link #cancelEnteringMatchPort} 로 간다.
     *
     * @param matchId 매치 ID
     * @return 종료된 매치. 없으면 빈 값
     */
    Optional<Match> forceEndMatchPort(Long matchId);
}
