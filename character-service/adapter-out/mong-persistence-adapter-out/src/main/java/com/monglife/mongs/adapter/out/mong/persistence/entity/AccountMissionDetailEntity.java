package com.monglife.mongs.adapter.out.mong.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DISTINCT 미션의 집계 대상 기록.
 *
 * <p>"서로 다른 음식 5종"의 진행도는 이 표의 행 수다. 같은 음식을 두 번 먹여도
 * 유니크 키에 막혀 행이 늘지 않는다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "mongs_account_mission_detail",
        uniqueConstraints = @UniqueConstraint(name = "uk_account_mission_detail", columnNames = { "account_mission_id", "detail_code" })
)
public class AccountMissionDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_mission_detail_id")
    private Long accountMissionDetailId;

    @Column(name = "account_mission_id", nullable = false)
    private Long accountMissionId;

    /** 음식 코드 / 아이템 코드 / 몽 코드 / 날짜 키 */
    @Column(name = "detail_code", length = 32, nullable = false)
    private String detailCode;

    @Builder
    public AccountMissionDetailEntity(Long accountMissionDetailId, Long accountMissionId, String detailCode) {
        this.accountMissionDetailId = accountMissionDetailId;
        this.accountMissionId = accountMissionId;
        this.detailCode = detailCode;
    }
}
