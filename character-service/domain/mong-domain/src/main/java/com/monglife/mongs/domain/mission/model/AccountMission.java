package com.monglife.mongs.domain.mission.model;

import com.monglife.mongs.domain.mission.enums.MissionStateCode;
import com.monglife.mongs.domain.mission.exception.AlreadyClaimedMissionException;
import com.monglife.mongs.domain.mission.exception.ForbiddenMissionException;
import com.monglife.mongs.domain.mission.exception.NotClaimableMissionException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 사용자별 미션 인스턴스. (계정 + 미션 + 주기 키) 하나당 한 행이다.
 *
 * <p>주기가 바뀌면 새 {@code cycleKey} 로 새 행이 생기고 옛 행은 그대로 남는다.
 * 초기화 배치가 없는 이유다.
 */
@Getter
@ToString
public class AccountMission {

    private final Long accountMissionId;

    private final Long accountId;

    private final Mission mission;

    private final String cycleKey;

    private Integer progressCount;

    private MissionStateCode stateCode;

    private LocalDateTime claimedAt;

    /**
     * DISTINCT 미션에서 이미 집계된 대상 코드들. COUNT/ACCUMULATE 미션에서는 비어 있다.
     */
    private final Set<String> detailCodes;

    /** 이번 호출로 진행도가 실제로 올랐는지. 안 올랐으면 어댑터가 저장을 건너뛴다 */
    private Boolean isProgressChange;

    /** 이번 호출로 새로 집계된 DISTINCT 대상 코드. 어댑터가 detail 행을 넣는 근거다 */
    private String addedDetailCode;

    /** 이번 호출로 CLAIMABLE 이 되었는지. 달성 알림을 붙일 자리다 */
    private Boolean isClaimableChange;

    @Builder
    public AccountMission(Long accountMissionId, Long accountId, Mission mission, String cycleKey, Integer progressCount, MissionStateCode stateCode, LocalDateTime claimedAt, Set<String> detailCodes) {
        this.accountMissionId = accountMissionId;
        this.accountId = accountId;
        this.mission = mission;
        this.cycleKey = cycleKey;
        this.progressCount = progressCount == null ? 0 : progressCount;
        this.stateCode = stateCode == null ? MissionStateCode.IN_PROGRESS : stateCode;
        this.claimedAt = claimedAt;
        this.detailCodes = detailCodes == null ? new HashSet<>() : new HashSet<>(detailCodes);
        this.isProgressChange = false;
        this.addedDetailCode = null;
        this.isClaimableChange = false;
    }

    /**
     * 미션 권한 확인
     * @param accountId 계정 ID
     * @return 사용자 미션 도메인 객체
     */
    public AccountMission verify(Long accountId) {

        if (!this.accountId.equals(accountId)) {
            throw new ForbiddenMissionException();
        }

        return this;
    }

    /**
     * 진행도 반영.
     *
     * <p>목표 타입에 따라 해석이 다르다.
     * <ul>
     *   <li>COUNT - 액션 1회당 1 증가. amount·detailCode 는 무시한다</li>
     *   <li>DISTINCT - 처음 보는 detailCode 일 때만 1 증가. 같은 음식을 두 번 먹여도 1이다</li>
     *   <li>ACCUMULATE - amount 만큼 증가</li>
     * </ul>
     *
     * <p>이미 CLAIMABLE / CLAIMED 인 미션은 더 올리지 않는다. 목표를 넘겨 쌓아 두면
     * 다음 주기에 이월되는 것처럼 보이는데, 주기마다 새 행이라 이월은 존재하지 않는다.
     *
     * @param amount ACCUMULATE 일 때 더할 값
     * @param detailCode DISTINCT 일 때 집계 대상 코드
     */
    public void increaseProgress(Integer amount, String detailCode) {

        this.isProgressChange = false;
        this.addedDetailCode = null;
        this.isClaimableChange = false;

        if (!MissionStateCode.IN_PROGRESS.equals(this.stateCode)) {
            return;
        }

        int increased = switch (this.mission.getGoalTypeCode()) {
            case COUNT -> 1;
            case ACCUMULATE -> amount == null ? 0 : amount;
            case DISTINCT -> {
                if (detailCode == null || this.detailCodes.contains(detailCode)) {
                    yield 0;
                }
                this.detailCodes.add(detailCode);
                this.addedDetailCode = detailCode;
                yield 1;
            }
        };

        if (increased <= 0) {
            return;
        }

        this.progressCount = Math.min(this.progressCount + increased, this.mission.getGoalCount());
        this.isProgressChange = true;

        if (this.progressCount >= this.mission.getGoalCount()) {
            this.stateCode = MissionStateCode.CLAIMABLE;
            this.isClaimableChange = true;
        }
    }

    /**
     * 리워드 수령 처리.
     *
     * <p>지급보다 먼저 부른다. 상태를 CLAIMED 로 못 박은 뒤 지급해야
     * 같은 트랜잭션 안에서 두 번 지급되는 경로가 생기지 않는다.
     *
     * @param claimedAt 수령 시각
     */
    public void claim(LocalDateTime claimedAt) {

        if (MissionStateCode.CLAIMED.equals(this.stateCode)) {
            throw new AlreadyClaimedMissionException();
        }

        if (!MissionStateCode.CLAIMABLE.equals(this.stateCode)) {
            throw new NotClaimableMissionException();
        }

        this.stateCode = MissionStateCode.CLAIMED;
        this.claimedAt = claimedAt;
    }

    public Set<String> getDetailCodes() {
        return Collections.unmodifiableSet(this.detailCodes);
    }
}
