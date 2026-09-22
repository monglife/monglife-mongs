package com.monglife.mongs.adapter.out.mong.persistence.entity;

import com.monglife.module.common.jpa.entity.BaseTimeEntity;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionStateCode;
import com.monglife.mongs.domain.mission.model.AccountMission;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 사용자별 미션 인스턴스.
 *
 * <p>주기 초기화 배치가 없다. cycle_key 가 유니크 키에 들어 있어 주기가 바뀌면
 * 새 행이 생기고 옛 행은 그대로 남는다 - 지울 것도 리셋할 것도 없다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners({ AuditingEntityListener.class })
@Table(
        name = "mongs_account_mission",
        uniqueConstraints = @UniqueConstraint(name = "uk_account_mission", columnNames = { "account_id", "mission_id", "cycle_key" }),
        indexes = @Index(name = "idx_account_mission_cycle", columnList = "account_id, cycle_key")
)
public class AccountMissionEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_mission_id")
    private Long accountMissionId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private MissionEntity mission;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_cycle_code", length = 16, nullable = false)
    private MissionCycleCode cycleCode;

    /** DAILY=20260917, WEEKLY=2026-W38, MONTHLY=202609 (KST 기준) */
    @Column(name = "cycle_key", length = 16, nullable = false)
    private String cycleKey;

    @Column(name = "progress_count", nullable = false)
    private Integer progressCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_code", length = 16, nullable = false)
    private MissionStateCode stateCode;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Builder
    public AccountMissionEntity(Long accountMissionId, Long accountId, MissionEntity mission, MissionCycleCode cycleCode, String cycleKey, Integer progressCount, MissionStateCode stateCode, LocalDateTime claimedAt) {
        this.accountMissionId = accountMissionId;
        this.accountId = accountId;
        this.mission = mission;
        this.cycleCode = cycleCode;
        this.cycleKey = cycleKey;
        this.progressCount = progressCount;
        this.stateCode = stateCode;
        this.claimedAt = claimedAt;
    }

    /**
     * 진행 상태 반영. detail 행은 어댑터가 따로 넣는다
     */
    public void update(AccountMission accountMission) {
        this.progressCount = accountMission.getProgressCount();
        this.stateCode = accountMission.getStateCode();
        this.claimedAt = accountMission.getClaimedAt();
    }

    /**
     * @param detailCodes DISTINCT 미션에서 이미 집계된 대상 코드들. COUNT/ACCUMULATE 면 비어 있다
     */
    public AccountMission toDomain(Collection<String> detailCodes) {
        return AccountMission.builder()
                .accountMissionId(this.accountMissionId)
                .accountId(this.accountId)
                .mission(this.mission.toDomain())
                .cycleKey(this.cycleKey)
                .progressCount(this.progressCount)
                .stateCode(this.stateCode)
                .claimedAt(this.claimedAt)
                .detailCodes(detailCodes == null ? Set.of() : Set.copyOf(detailCodes))
                .build();
    }
}
