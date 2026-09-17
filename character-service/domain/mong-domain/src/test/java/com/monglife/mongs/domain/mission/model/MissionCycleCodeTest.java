package com.monglife.mongs.domain.mission.model;

import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MissionCycleCodeTest {

    @Nested
    @DisplayName("일간 주기 키 단위 테스트")
    class DailyCycleKey {

        @Test
        @DisplayName("날짜를 yyyyMMdd 로 만든다.")
        void dailyCycleKey() {
            // arrange
            final LocalDate date = LocalDate.of(2026, 9, 17);

            // act
            final String expected = MissionCycleCode.DAILY.cycleKey(date);

            // assert
            assertEquals("20260917", expected);
        }

        @Test
        @DisplayName("월과 일을 두 자리로 채운다.")
        void dailyCycleKeyPadded() {
            // arrange
            final LocalDate date = LocalDate.of(2026, 1, 5);

            // act
            final String expected = MissionCycleCode.DAILY.cycleKey(date);

            // assert
            assertEquals("20260105", expected);
        }

        @Test
        @DisplayName("날이 바뀌면 키도 바뀐다.")
        void dailyCycleKeyChangesOverDay() {
            // arrange
            final LocalDate date = LocalDate.of(2026, 9, 17);

            // act & assert
            assertNotEquals(
                    MissionCycleCode.DAILY.cycleKey(date),
                    MissionCycleCode.DAILY.cycleKey(date.plusDays(1)));
        }
    }

    @Nested
    @DisplayName("주간 주기 키 단위 테스트")
    class WeeklyCycleKey {

        @Test
        @DisplayName("같은 주의 월요일과 일요일은 같은 키를 가진다.")
        void weeklyCycleKeyWithinSameWeek() {
            // arrange - 2026-09-14(월) ~ 2026-09-20(일)
            final LocalDate monday = LocalDate.of(2026, 9, 14);
            final LocalDate sunday = LocalDate.of(2026, 9, 20);

            // act & assert
            assertEquals(
                    MissionCycleCode.WEEKLY.cycleKey(monday),
                    MissionCycleCode.WEEKLY.cycleKey(sunday));
        }

        @Test
        @DisplayName("일요일과 다음 월요일은 다른 키를 가진다.")
        void weeklyCycleKeyAcrossWeek() {
            // arrange
            final LocalDate sunday = LocalDate.of(2026, 9, 20);
            final LocalDate nextMonday = LocalDate.of(2026, 9, 21);

            // act & assert
            assertNotEquals(
                    MissionCycleCode.WEEKLY.cycleKey(sunday),
                    MissionCycleCode.WEEKLY.cycleKey(nextMonday));
        }

        @Test
        @DisplayName("주차는 두 자리로 채운다.")
        void weeklyCycleKeyPadded() {
            // arrange - 2026-01-05 는 2026년 2주차 월요일
            final LocalDate date = LocalDate.of(2026, 1, 5);

            // act
            final String expected = MissionCycleCode.WEEKLY.cycleKey(date);

            // assert
            assertEquals("2026-W02", expected);
        }

        @Test
        @DisplayName("연말이 다음 해 1주차에 속하면 연도도 다음 해로 적는다.")
        void weeklyCycleKeyOnYearBoundary() {
            // arrange - 2025-12-29(월) 는 ISO 기준 2026년 1주차다.
            // date.getYear() 를 쓰면 2025-W01 이 되어 2025년 1월 첫 주와 키가 겹친다.
            final LocalDate date = LocalDate.of(2025, 12, 29);

            // act
            final String expected = MissionCycleCode.WEEKLY.cycleKey(date);

            // assert
            assertEquals("2026-W01", expected);
            assertNotEquals(MissionCycleCode.WEEKLY.cycleKey(LocalDate.of(2025, 1, 2)), expected);
        }
    }

    @Nested
    @DisplayName("월간 주기 키 단위 테스트")
    class MonthlyCycleKey {

        @Test
        @DisplayName("날짜를 yyyyMM 으로 만든다.")
        void monthlyCycleKey() {
            // arrange
            final LocalDate date = LocalDate.of(2026, 9, 17);

            // act
            final String expected = MissionCycleCode.MONTHLY.cycleKey(date);

            // assert
            assertEquals("202609", expected);
        }

        @Test
        @DisplayName("같은 달의 1일과 말일은 같은 키를 가진다.")
        void monthlyCycleKeyWithinSameMonth() {
            // arrange
            final LocalDate first = LocalDate.of(2026, 2, 1);
            final LocalDate last = LocalDate.of(2026, 2, 28);

            // act & assert
            assertEquals(
                    MissionCycleCode.MONTHLY.cycleKey(first),
                    MissionCycleCode.MONTHLY.cycleKey(last));
        }
    }
}
