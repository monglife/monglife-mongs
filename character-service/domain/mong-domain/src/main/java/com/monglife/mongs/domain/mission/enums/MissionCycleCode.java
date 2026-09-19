package com.monglife.mongs.domain.mission.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.IsoFields;

@Getter
@AllArgsConstructor
public enum MissionCycleCode {
    DAILY("일간 미션"),
    WEEKLY("주간 미션"),
    MONTHLY("월간 미션"),
    ;

    private final String description;

    /**
     * 주기 키 생성.
     *
     * <p>주기 초기화를 배치로 돌리지 않고 이 키로 가른다. 사용자별 미션 행의 유니크 키에 들어가므로
     * 주기가 바뀌는 순간 새 행이 생기고 옛 행은 그대로 남는다 - 초기화할 대상이 없다.
     *
     * <p>날짜를 인자로 받는다. {@code LocalDate.now()} 를 안에서 부르면 주차 경계를 테스트할 수 없다.
     * 호출 측이 서비스 기준 시간대(KST)의 오늘을 넘기는 책임을 진다.
     *
     * @param date 기준 날짜 (서비스 기준 시간대의 날짜)
     * @return 주기 키. DAILY=20260917, WEEKLY=2026-W38, MONTHLY=202609
     */
    public String cycleKey(LocalDate date) {

        return switch (this) {
            // 주(week)는 ISO-8601 기준이다. 월요일 시작이고, 연말 며칠이 다음 해 1주차에 붙을 수 있어
            // 연도도 WEEK_BASED_YEAR 를 쓴다. date.getYear() 를 쓰면 12월 31일이 2026-W01 이 되어
            // 한 해 전 1월 첫 주와 키가 겹친다.
            case WEEKLY -> String.format("%d-W%02d",
                    date.get(IsoFields.WEEK_BASED_YEAR),
                    date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case MONTHLY -> String.format("%d%02d", date.getYear(), date.getMonthValue());
            case DAILY -> String.format("%d%02d%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        };
    }
}
