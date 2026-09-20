package com.monglife.mongs.domain.battle.model;

import com.monglife.mongs.domain.battle.enums.MatchPickCode;
import com.monglife.mongs.domain.battle.enums.MatchStateCode;
import com.monglife.mongs.domain.battle.exception.AlreadyExistsMatchPickException;
import com.monglife.mongs.domain.battle.exception.AlreadyStartMatchException;
import com.monglife.mongs.domain.battle.exception.NotEnteringMatchException;
import com.monglife.mongs.domain.battle.exception.NotExistsMatchPlayerException;
import com.monglife.mongs.domain.battle.exception.NotPickedAllMatchPlayersException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@ToString
public class Match {

    // 매치 완료 시 보상 경험치
    private static final double REWARD_EXP = 10D;
    // 매치 승리 시 보상 페이 포인트
    private static final int REWARD_PAY_POINT = 200;
    // 매치 참여 시 배팅 페이 포인트
    private static final int BETTING_PAY_POINT = 50;
    // 매치 생성 시, 초기 라운드 수
    private static final int INIT_ROUND = 0;
    // 매치 생성 시, 초기 상태 코드
    private static final MatchStateCode INIT_MATCH_STATE_CODE = MatchStateCode.ENTERING;
    // 매치 최대 라운드 수
    private static final int MAX_ROUND = 10;
    /**
     * 입장 기한 (초). 이 시간이 지나도록 ENTERING 이면 취소하고 배팅을 돌려준다.
     *
     * <p>정상 입장은 매칭 알림 후 2초 안에 끝난다(앱이 토픽을 구독하고 바로 발행한다).
     * 대기열의 봇 매칭 유예가 10초인 것과 견줘 넉넉하게 잡았다.
     */
    private static final long ENTER_EXPIRED_SECONDS = 30L;

    private final Long matchId;

    private final Integer maxRound;

    private final List<MatchPlayer> matchPlayers;

    private final List<MatchPick> matchPicks;

    private Integer round;

    private MatchStateCode stateCode;

    @Builder
    public Match(Long matchId, Integer maxRound, Integer round, MatchStateCode stateCode, List<MatchPlayer> matchPlayers, List<MatchPick> matchPicks) {
        this.matchId = matchId;
        this.round = round;
        this.maxRound = maxRound;
        this.stateCode = stateCode;
        this.matchPlayers = matchPlayers == null ? new ArrayList<>() : matchPlayers;
        this.matchPicks = matchPicks == null ? new ArrayList<>() : matchPicks;
    }

    /**
     * 매치 시작
     */
    private void start() {
        this.stateCode = MatchStateCode.PROCESS;
        this.round = 0;
    }

    /**
     * 매치 종료
     *
     */
    private void end() {
        this.stateCode = MatchStateCode.END;
    }

    /**
     * 관리자 강제 종료. 멈춘 매치를 END 로 마감한다. 보상은 주지 않는다.
     *
     * <p>입장 대기 중(ENTERING)인 매치라면 이걸 쓰지 말고 {@link #cancelEntering()} 을 써야 한다.
     * 아무도 싸우지 않았으므로 배팅을 돌려줘야 하고, 그 경로는 CANCELED 로 간다.
     */
    public void adminEnd() {
        this.end();
    }

    /**
     * 입장 기한 초과로 취소. 참가비 환불 대상이 된다.
     *
     * <p>ENTERING 이 아니면 던진다. 스위퍼가 후보를 고른 뒤 행을 잠그기까지의 틈에 마지막
     * 플레이어가 입장해 매치가 시작될 수 있는데, 그때 살아 있는 매치를 닫으면 안 된다.
     */
    public void cancelEntering() {
        if (!this.isEntering()) {
            throw new NotEnteringMatchException();
        }

        this.stateCode = MatchStateCode.CANCELED;
    }

    /**
     * 라운드 변경
     */
    private void nextRound() {
        // 매치 선택이 완료 되지 않은 경우
        if (!this.isPickedAllMatchPlayers()) {
            throw new NotPickedAllMatchPlayersException();
        }

        // 봇 매치 플레이어 매치 선택 생성
        this.matchPlayers.stream()
                .filter(MatchPlayer::getIsBot)
                .forEach(matchPlayer -> {
                    // 매치 선택 중복 방지
                    if (!this.isPickedMatchPlayerInCurrentRound(matchPlayer.getPlayerId())) {
                        this.matchPicks.add(MatchPick.generateMatchPick(this.getCurrentRound(), matchPlayer, this.matchPlayers));
                    }
                });

        // 퇴장한 매치 플레이어 매치 선택 생성 (방어 선택 고정)
        this.matchPlayers.stream()
                .filter(matchPlayer -> !matchPlayer.getIsBot() && !matchPlayer.getIsEnter())
                .forEach(matchPlayer -> {
                    // 매치 선택 중복 방지
                    if (!this.isPickedMatchPlayerInCurrentRound(matchPlayer.getPlayerId())) {
                        this.matchPicks.add(MatchPick.builder()
                                .matchPlayer(matchPlayer)
                                .targetMatchPlayer(matchPlayer)
                                .round(this.getCurrentRound())
                                .pickCode(MatchPickCode.MATCH_PICK_DEFENCE)
                                .pickValue(matchPlayer.getDefence())
                                .build());
                    }
                });

        // 매치 플레이어 선택 적용 (공격, 방어, 회복)
        this.matchPicks.stream()
                .filter(matchPick -> matchPick.getRound() == this.getCurrentRound())
                .forEach(matchPick -> {
            switch (matchPick.getPickCode()) {
                case MATCH_PICK_DEFENCE -> matchPick.getTargetMatchPlayer().defence();
                case MATCH_PICK_HEAL -> matchPick.getTargetMatchPlayer().heal(matchPick.getPickValue());
                case MATCH_PICK_ATTACK -> matchPick.getTargetMatchPlayer().damage(matchPick.getPickValue());
            }
        });

        // 피해, 회복 수치 적용
        this.matchPlayers.forEach(matchPlayer -> {
            matchPlayer.applyDamageAndRecovery();

            // 매치 플레이어 사망한 경우 매치 종료
            if (matchPlayer.isDead()) {
                this.end();
            }
        });

        // 라운드 수 증가
        this.round = Math.min(this.round + 1, this.maxRound);

        // 마지막 라운드 경우 매치 종료
        if (this.maxRound <= this.round) {
            this.end();
        }
    }

    /**
     * 매치 플레이어 입장
     * @param playerId 매치 플레이어 ID
     */
    public void enterMatchPlayer(String playerId) {
        // 이미 매치가 시작한 경우
        if (this.isStart()) {
            throw new AlreadyStartMatchException();
        }

        // 매치 플레이어 입장 처리
        for (MatchPlayer matchPlayer : matchPlayers) {
            if (matchPlayer.getPlayerId().equals(playerId)) {
                matchPlayer.enter();
                break;
            }
        }

        // 모든 매치 플레이어 입장 완료인 경우 매치 시작
        if (this.isAllMatchPlayersEntered()) {
            this.start();
        }
    }

    /**
     * 매치 라운드 선택
     * @param matchPick 매치 선택 도메인 객체
     * @return 다음 라운드 진행 여부
     */
    public boolean pickMatchPlayer(MatchPick matchPick) {

        // 현재 라운드 매치 선택 여부 확인
        if (this.isPickedMatchPlayerInCurrentRound(matchPick.getMatchPlayer().getPlayerId())) {
            throw new AlreadyExistsMatchPickException();
        }

        this.matchPicks.add(matchPick);

        // 모든 매치 플레이어 선택 완료인 경우 다음 라운드 진행
        if (this.isPickedAllMatchPlayers()) {
            this.nextRound();
            return true;
        }

        return false;
    }

    /**
     * 매치 플레이어 퇴장
     * @param playerId 매치 플레이어 ID
     */
    public void exitMatchPlayer(String playerId) {
        // 매치 플레이어 퇴장 처리
        for (MatchPlayer matchPlayer : matchPlayers) {
            if (matchPlayer.getPlayerId().equals(playerId)) {
                matchPlayer.exit();
                break;
            }
        }

        // 매치 플레이어 1명 or 모든 플레이어 퇴장 상태인 경우
        if (this.isAllMatchPlayersExited()) {
            // 매치 종료
            this.end();
        }
    }

    /**
     * 승리한 매치 플레이어 조회
     * @return 승리한 매치 플레이어 도메인 객체
     */
    public MatchPlayer getWinner() {
        // 나간 배틀 플레이어
        List<MatchPlayer> rankedMatchPlayers = this.matchPlayers.stream()
                .filter(matchPlayer -> !matchPlayer.getIsEnter())
                .sorted((mp1, mp2) -> {
                    if (mp1.getHp().equals(mp2.getHp())) {
                        // 한 번도 입장하지 않은 플레이어는 exitedAt 이 null 이다. 사람 둘이 모두
                        // 입장하지 않은 채 HP 가 같으면 그냥 비교하다 터진다. 그런 플레이어를
                        // 가장 낮은 순위로 둔다 - 나가지도 않은 쪽이 이길 이유가 없다.
                        return Comparator.nullsFirst(LocalDateTime::compareTo)
                                .compare(mp1.getExitedAt(), mp2.getExitedAt());
                    }
                    return mp1.getHp().compareTo(mp2.getHp());
                })
                .collect(Collectors.toList());

        // 나가지 않은 배틀 플레이어
        this.matchPlayers.stream()
                .filter(MatchPlayer::getIsEnter)
                // 역순 정렬
                .sorted((mp1, mp2) -> {
                    if (mp1.getHp().equals(mp2.getHp())) {
                        return mp2.getEnteredAt().compareTo(mp1.getEnteredAt());
                    }
                    return mp1.getHp().compareTo(mp2.getHp());
                })
                // 순위 리스트 앞에서 부터 삽입 (하위 등수 부터 저장)
                .forEachOrdered(matchPlayerEntity -> rankedMatchPlayers.add(0, matchPlayerEntity));

        // 매치 플레이어 없는 경우 예외
        return rankedMatchPlayers.stream().findFirst()
                .orElseThrow(NotExistsMatchPlayerException::new);
    }

    /**
     * 매치 플레이어 조회
     * @param playerId 매치 플레이어 ID
     * @return 매치 플레이어 도메인 객체
     */
    public MatchPlayer getMatchPlayer(String playerId) {
        for (MatchPlayer matchPlayer : matchPlayers) {
            if (matchPlayer.getPlayerId().equals(playerId)) {
                return matchPlayer;
            }
        }

        throw new NotExistsMatchPlayerException();
    }


    /**
     * 현재 진행중인 라운드 조회
     * @return 현재 라운드
     */
    public int getCurrentRound() {
        return Math.min(this.round + 1, this.maxRound);
    }

    /**
     * 매치 시작 여부 확인
     * @return 매치 시작 여부
     */
    public boolean isStart() {
        return MatchStateCode.PROCESS.equals(this.stateCode);
    }

    /**
     * 매치 종료 여부 확인. <b>치러진 경기만</b> 해당한다 - 취소된 매치는 false 다.
     *
     * <p>승자 조회가 이 값으로 갈린다. 취소된 매치에서 승자를 뽑으면 아무도 입장하지 않은
     * 채로 봇이 이겼다고 나오거나, 사람 둘이 동률이라 정렬하다 터진다.
     * @return 매치 종료 여부
     */
    public boolean isEnd() {
        return MatchStateCode.END.equals(this.stateCode);
    }

    /**
     * 입장 대기 중 여부 확인
     * @return 입장 대기 중 여부
     */
    public boolean isEntering() {
        return MatchStateCode.ENTERING.equals(this.stateCode);
    }

    /**
     * 더 진행할 수 없는 상태인지 (종료 또는 취소).
     *
     * <p>입장·선택·퇴장을 막을 때는 {@link #isEnd()} 가 아니라 이걸 봐야 한다. 취소된 매치에
     * 퇴장이 들어오면 {@code isEnd()} 기준으로는 통과해 버려 승리 보상까지 지급된다.
     * @return 진행 불가 여부
     */
    public boolean isTerminal() {
        return this.isEnd() || MatchStateCode.CANCELED.equals(this.stateCode);
    }

    /**
     * 마지막 라운드 여부 확인. 취소도 포함한다 - 앱이 더 기다리지 않고 화면을 닫게 한다.
     * @return 마지막 라운드 여부
     */
    public boolean isLastRound() {
        return this.isTerminal() || this.maxRound.equals(this.round);
    }

    /**
     * 모든 매치 플레이어 매치 선택 여부 확인
     * @return 매치 선택 여부 확인
     */
    public boolean isPickedAllMatchPlayers() {
        // 선택한 매치 플레이어 ID 목록
        Set<String> pickedMatchPlayerIds = this.matchPicks.stream()
                .filter(matchPick -> matchPick.getRound() == this.getCurrentRound())
                .map(matchPick -> matchPick.getMatchPlayer().getPlayerId())
                .collect(Collectors.toSet());

        // 봇이 아닌 매치 플레이어 또는 퇴장한 매치 플레이어 중, 선택 완료인 경우가 아닌 매치 플레이어 존재 여부 확인
        return this.matchPlayers.stream()
                .filter(matchPlayer -> !matchPlayer.getIsBot() && matchPlayer.getIsEnter())
                .filter(matchPlayer -> !pickedMatchPlayerIds.contains(matchPlayer.getPlayerId()))
                .toList()
                .isEmpty();
    }

    /**
     * 현재 라운드 상 매치 선택 완료 여부
     * @param playerId 매치 플레이어 ID
     * @return 매치 선택 완료 여부
     */
    public boolean isPickedMatchPlayerInCurrentRound(String playerId) {
        for (MatchPick matchPick : this.matchPicks) {
            if (matchPick.getRound() != this.getCurrentRound()) continue;
            if (matchPick.getMatchPlayer().getPlayerId().equals(playerId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 모든 매치 플레이어 입장 여부 확인
     * @return 모든 매치 플레이어 입장 여부
     */
    public boolean isAllMatchPlayersEntered() {
        for (MatchPlayer matchPlayer : matchPlayers) {
            if (!matchPlayer.getIsEnter()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 모든 매치 플레이어 퇴장 or 1명의 매치 플레이어 잔류 여부 확인
     * @return 매치 플레이어 퇴장 여부
     */
    public boolean isAllMatchPlayersExited() {
        int matchPlayersCount = 0;

        for (MatchPlayer matchPlayer : matchPlayers) {
            if (matchPlayer.getIsEnter()) {
                matchPlayersCount++;
            }
        }

        return matchPlayersCount <= 1;
    }

    /**
     * 보상 경험치 수치 조회
     * @return 보상 경험치 수치
     */
    public static double getRewardExp() {
        return REWARD_EXP;
    }

    /**
     * 보상 페이 포인트 조회
     * @return 보상 페이 포인트
     */
    public static int getRewardPayPoint() {
        return REWARD_PAY_POINT;
    }

    /**
     * 배팅 페이 포인트 조회
     * @return 배팅 페이 포인트
     */
    public static int getBettingPayPoint() {
        return BETTING_PAY_POINT;
    }

    /**
     * 매치 초기 라운드 값 조회
     * @return 초기 라운드 값
     */
    public static int getInitRound() {
        return INIT_ROUND;
    }

    /**
     * 매치 초기 상태 값 조회
     * @return 초기 상태 값
     */
    public static MatchStateCode getInitMatchStateCode() {
        return INIT_MATCH_STATE_CODE;
    }

    /**
     * 매치 최대 라운드 수 조회
     * @return 매치 최대 라운드 수
     */
    public static int getInitMaxRound() {
        return MAX_ROUND;
    }

    /**
     * 입장 기한 조회 (초)
     * @return 입장 기한 초
     */
    public static long getEnterExpiredSeconds() {
        return ENTER_EXPIRED_SECONDS;
    }
}
