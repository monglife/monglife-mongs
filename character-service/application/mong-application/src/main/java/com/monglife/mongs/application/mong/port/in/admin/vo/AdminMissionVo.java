package com.monglife.mongs.application.mong.port.in.admin.vo;

import com.monglife.mongs.domain.mission.model.Mission;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 관리자 화면용 미션. 마스터 값에 "지금 게시 중인가"와 그 주기 구간을 얹는다.
 *
 * <p>게시 여부를 화면에서 계산하지 않고 서버가 실어 보낸다 - 주간 경계가 서비스 기준
 * 시간대(KST)의 월요일이라 브라우저 시간대로 재면 하루씩 어긋난다.
 */
@Getter
public class AdminMissionVo {

    private final Mission mission;

    /** 지금 사용자에게 나가는 중인가. 이때는 수정·삭제가 막힌다 */
    private final Boolean isPublished;

    /** 이번 주기 구간. 게시 중이면 이 구간 동안 잠긴다 */
    private final LocalDate periodStart;

    private final LocalDate periodEnd;

    /** 이번 주기에 당첨된 로테이션 그룹. 활성 미션이 없으면 null */
    private final Integer currentRotationGroup;

    @Builder
    public AdminMissionVo(Mission mission, Boolean isPublished, LocalDate periodStart, LocalDate periodEnd, Integer currentRotationGroup) {
        this.mission = mission;
        this.isPublished = isPublished;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.currentRotationGroup = currentRotationGroup;
    }
}
