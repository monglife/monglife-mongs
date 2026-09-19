package com.monglife.mongs.domain.mission.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;

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

    /**
     * 주기 시작일 (그 주기의 첫날).
     *
     * <p>주간은 ISO-8601 을 따라 월요일이다. cycleKey 와 같은 경계를 써야 한다 -
     * 한쪽만 일요일 시작이 되면 게시 기간 표시와 실제 초기화 시점이 어긋난다.
     */
    public LocalDate periodStart(LocalDate date) {

        return switch (this) {
            case DAILY -> date;
            case WEEKLY -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY -> date.withDayOfMonth(1);
        };
    }

    /** 주기 종료일 (다음 주기 첫날 - 1일). 화면에 "9/21 ~ 9/27" 처럼 닫힌 구간으로 보여 주려고 쓴다 */
    public LocalDate periodEnd(LocalDate date) {

        LocalDate start = this.periodStart(date);

        return switch (this) {
            case DAILY -> start;
            case WEEKLY -> start.plusWeeks(1).minusDays(1);
            case MONTHLY -> start.plusMonths(1).minusDays(1);
        };
    }

    /**
     * 주기 일련번호. 로테이션 그룹을 고르는 데 쓴다.
     *
     * <p>끊기지 않고 1씩 오르는 값이어야 한다. {@code WEEK_OF_WEEK_BASED_YEAR} 로 만들면
     * 52주인 해와 53주인 해가 섞여 경계에서 그룹이 하나 건너뛴다. 그래서 주간은
     * 기준 월요일(1970-01-05)로부터의 주 수를 센다.
     *
     * @param date 기준 날짜 (서비스 기준 시간대의 날짜)
     * @return 0 이상 단조 증가하는 주기 번호
     */
    public long periodIndex(LocalDate date) {

        return switch (this) {
            case DAILY -> date.toEpochDay();
            case WEEKLY -> ChronoUnit.WEEKS.between(EPOCH_MONDAY, this.periodStart(date));
            case MONTHLY -> (long) date.getYear() * 12 + date.getMonthValue() - 1;
        };
    }

    /** 1970-01-05 는 월요일이다. 주 단위 일련번호의 기준점 */
    private static final LocalDate EPOCH_MONDAY = LocalDate.of(1970, 1, 5);
}
