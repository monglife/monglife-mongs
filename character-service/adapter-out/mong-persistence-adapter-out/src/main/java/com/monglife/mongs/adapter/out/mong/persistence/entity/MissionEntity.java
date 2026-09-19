package com.monglife.mongs.adapter.out.mong.persistence.entity;

import com.monglife.module.common.jpa.entity.BaseTimeEntity;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.enums.MissionGoalTypeCode;
import com.monglife.mongs.domain.mission.model.Mission;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 미션 마스터.
 *
 * <p>다른 마스터(mongs_food 등)와 달리 monglife_comn_code 에 FK 를 걸지 않는다.
 * 공통 코드는 mongs_inventory · mongs_order 처럼 사용자 소유 행에 FK 로 박히는 코드용이고,
 * 미션 코드는 어디에도 참조되지 않으며 제목/설명도 미션 고유 문구다.
 * 공통 코드 시드 파일은 mong·member 양쪽에 같은 내용으로 유지해야 해서 넣을수록 손해다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners({ AuditingEntityListener.class })
@Table(
        name = "mongs_mission",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mission_code", columnNames = { "mission_code" }),
                // 겹침 방지. 같은 (액션, 목표 타입, 목표치) 는 한 번만 존재한다.
                // 주기까지 넣지 않는 것이 핵심이다 - 일간 "밥 5번"과 주간 "밥 5번"이 동시에 못 생긴다.
                @UniqueConstraint(name = "uk_mission_goal", columnNames = { "mission_action_code", "mission_goal_type_code", "goal_count" })
        }
)
public class MissionEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long missionId;

    @Column(name = "mission_code", length = 32, nullable = false)
    private String missionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_cycle_code", length = 16, nullable = false)
    private MissionCycleCode cycleCode;

    /**
     * 운영 DDL 은 이 컬럼을 varchar(32) 로 만든다(docs/ddl 참고).
     *
     * <p>Hibernate 6 + MySQLDialect 는 {@code @Enumerated(STRING)} 을 네이티브 MySQL ENUM 으로 만들고
     * columnDefinition 이나 @JdbcTypeCode 로도 바뀌지 않는다. 하지만 stg/prd 는 hbm2ddl.auto 가 none 이라
     * 실제 스키마는 손으로 넣는 DDL 이다. 거기서 varchar 로 두면 액션 코드를 추가해도 운영 ALTER 가 필요 없다.
     * local/dev 는 create-drop/update 라 알아서 따라온다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "mission_action_code", length = 32, nullable = false)
    private MissionActionCode actionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_goal_type_code", length = 16, nullable = false)
    private MissionGoalTypeCode goalTypeCode;

    @Column(name = "title", length = 64, nullable = false)
    private String title;

    @Column(name = "description", length = 128)
    private String description;

    @Column(name = "goal_count", nullable = false)
    private Integer goalCount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @OneToMany(mappedBy = "mission", fetch = FetchType.LAZY)
    private List<MissionRewardEntity> rewards = new ArrayList<>();

    @Builder
    public MissionEntity(Long missionId, String missionCode, MissionCycleCode cycleCode, MissionActionCode actionCode, MissionGoalTypeCode goalTypeCode, String title, String description, Integer goalCount, Boolean isActive, Integer sortOrder) {
        this.missionId = missionId;
        this.missionCode = missionCode;
        this.cycleCode = cycleCode;
        this.actionCode = actionCode;
        this.goalTypeCode = goalTypeCode;
        this.title = title;
        this.description = description;
        this.goalCount = goalCount;
        this.isActive = isActive;
        this.sortOrder = sortOrder;
    }

    /**
     * 리워드 연관 동기화.
     *
     * <p>새로 영속화한 엔티티의 {@code rewards} 는 필드 초기화된 빈 리스트라 이미 "초기화된" 상태다.
     * 그래서 같은 영속성 컨텍스트 안에서는 fetch join 으로 다시 읽어도 채워지지 않는다.
     * 등록 직후 응답을 만들려면 이쪽에서 직접 넣어 줘야 한다.
     */
    public void addReward(MissionRewardEntity reward) {
        this.rewards.add(reward);
    }

    public Mission toDomain() {
        return Mission.builder()
                .missionId(this.missionId)
                .missionCode(this.missionCode)
                .cycleCode(this.cycleCode)
                .actionCode(this.actionCode)
                .goalTypeCode(this.goalTypeCode)
                .title(this.title)
                .description(this.description)
                .goalCount(this.goalCount)
                .isActive(this.isActive)
                .sortOrder(this.sortOrder)
                .rewards(this.rewards.stream().map(MissionRewardEntity::toDomain).toList())
                .build();
    }
}
