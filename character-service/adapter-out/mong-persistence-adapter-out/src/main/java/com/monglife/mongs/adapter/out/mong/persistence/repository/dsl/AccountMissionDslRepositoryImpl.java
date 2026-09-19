package com.monglife.mongs.adapter.out.mong.persistence.repository.dsl;

import com.monglife.mongs.adapter.out.mong.persistence.entity.AccountMissionEntity;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionStateCode;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.monglife.mongs.adapter.out.mong.persistence.entity.QAccountMissionEntity.accountMissionEntity;
import static com.monglife.mongs.adapter.out.mong.persistence.entity.QMissionEntity.missionEntity;

@Repository
public class AccountMissionDslRepositoryImpl implements AccountMissionDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public AccountMissionDslRepositoryImpl(@Qualifier("mongJpaQueryFactory") JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    /**
     * 진행도를 올릴 행만 잠그고 가져온다.
     *
     * <p>DISTINCT 미션의 진행도는 detail 행 수라, 같은 계정의 동시 요청이 각자 읽고 각자 쓰면
     * 한쪽이 덮여 사라진다. 이 잠금이 detail 삽입까지 직렬화하므로 detail 쪽에는 별도 방어가 필요 없다.
     *
     * <p>fetch join 을 쓰지 않는다. 잠금과 함께 걸면 조인된 마스터 행까지 잠근다 -
     * 미션 마스터는 모든 사용자가 공유해서 그러면 서로를 막는다.
     */
    @Override
    public List<AccountMissionEntity> findForUpdate(Long accountId, Collection<String> cycleKeys, Collection<MissionActionCode> actionCodes) {
        return jpaQueryFactory.selectFrom(accountMissionEntity)
                .join(accountMissionEntity.mission, missionEntity)
                .where(accountMissionEntity.accountId.eq(accountId)
                        .and(accountMissionEntity.cycleKey.in(cycleKeys))
                        .and(accountMissionEntity.stateCode.eq(MissionStateCode.IN_PROGRESS))
                        .and(missionEntity.actionCode.in(actionCodes)))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetch();
    }

    @Override
    public Optional<AccountMissionEntity> findByIdWithLock(Long accountMissionId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(accountMissionEntity)
                .where(accountMissionEntity.accountMissionId.eq(accountMissionId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne());
    }
}
