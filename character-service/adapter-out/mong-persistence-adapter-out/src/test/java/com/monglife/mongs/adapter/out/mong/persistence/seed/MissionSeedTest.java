package com.monglife.mongs.adapter.out.mong.persistence.seed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 미션 시드 데이터 검증.
 *
 * <p>겹침 방지 규칙(일간/주간/월간이 서로 같은 미션을 쓰지 않는다)은 DB 유니크 키와 관리자 등록 검증으로
 * 막지만, 시드 자체가 규칙을 어기고 들어오면 그 방어가 뚫린 채로 배포된다. 여기서 한 번 더 본다.
 *
 * <p>SQL 은 configs 서브모듈에 있고 copyPrivate 가 리소스로 복사한다. CI 는 테스트 전에 그 태스크를 돈다.
 */
class MissionSeedTest {

    /** (id, code, cycle, action, goalType) - 제목·설명은 한글이라 앞쪽 필드만 뽑는다 */
    private static final Pattern MISSION = Pattern.compile(
            "\\(\\s*(\\d+),\\s*'([^']+)',\\s*'([A-Z_]+)',\\s*'([A-Z_]+)',\\s*'([A-Z_]+)',");

    /** 뒤쪽의 goal_count, is_active, sort_order */
    private static final Pattern MISSION_TAIL = Pattern.compile(
            ",(\\d+),(\\d+),(\\d+),NOW\\(\\),NOW\\(\\)\\)");

    private static final Pattern REWARD = Pattern.compile(
            "\\(\\s*(\\d+),\\s*(\\d+),\\s*'([A-Z_]+)',\\s*(NULL|'[^']*'),\\s*(NULL|'[^']*'),\\s*(\\d+)\\s*\\)");

    private static final int DAILY_PICK_COUNT = 5;

    private record Seed(long missionId, String missionCode, String cycleCode, String actionCode, String goalTypeCode, int goalCount) {}

    private record Reward(long missionId, String rewardTypeCode, String rewardCode, String inventoryTypeCode, int amount) {}

    private static String read(String name) {
        try (InputStream in = MissionSeedTest.class.getClassLoader().getResourceAsStream(name)) {
            assertNotNull(in, name + " 이 클래스패스에 없다. ./gradlew copyPrivate 를 먼저 돌려야 한다.");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<Seed> missions() {

        String sql = read("mongs_mission.sql");
        List<Seed> seeds = new ArrayList<>();

        Matcher head = MISSION.matcher(sql);
        Matcher tail = MISSION_TAIL.matcher(sql);

        while (head.find()) {
            assertTrue(tail.find(head.end()), "미션 행의 뒤쪽 필드를 읽지 못했다: " + head.group(2));
            seeds.add(new Seed(
                    Long.parseLong(head.group(1)), head.group(2), head.group(3), head.group(4), head.group(5),
                    Integer.parseInt(tail.group(1))));
        }

        return seeds;
    }

    private static List<Reward> rewards() {

        String sql = read("mongs_mission_reward.sql");
        List<Reward> rewards = new ArrayList<>();

        Matcher matcher = REWARD.matcher(sql);
        while (matcher.find()) {
            rewards.add(new Reward(
                    Long.parseLong(matcher.group(2)),
                    matcher.group(3),
                    unquote(matcher.group(4)),
                    unquote(matcher.group(5)),
                    Integer.parseInt(matcher.group(6))));
        }

        return rewards;
    }

    private static String unquote(String value) {
        return "NULL".equals(value) ? null : value.substring(1, value.length() - 1);
    }

    private static Set<String> codesOf(String sqlName, String prefix) {

        Set<String> codes = new HashSet<>();
        Matcher matcher = Pattern.compile("'(" + prefix + "\\d+)'").matcher(read(sqlName));
        while (matcher.find()) {
            codes.add(matcher.group(1));
        }

        return codes;
    }

    @Nested
    @DisplayName("미션 시드 구성 단위 테스트")
    class MissionSeed {

        @Test
        @DisplayName("일간 20개, 주간 10개, 월간 10개가 들어 있다.")
        void missionCountPerCycle() {
            // arrange
            List<Seed> seeds = missions();

            // act & assert
            assertEquals(40, seeds.size());
            assertEquals(20, seeds.stream().filter(seed -> seed.cycleCode().equals("DAILY")).count());
            assertEquals(10, seeds.stream().filter(seed -> seed.cycleCode().equals("WEEKLY")).count());
            assertEquals(10, seeds.stream().filter(seed -> seed.cycleCode().equals("MONTHLY")).count());
        }

        @Test
        @DisplayName("미션 코드가 중복되지 않는다.")
        void missionCodeIsUnique() {
            // arrange
            List<Seed> seeds = missions();

            // act & assert
            assertEquals(seeds.size(), seeds.stream().map(Seed::missionCode).distinct().count());
        }

        @Test
        @DisplayName("같은 액션·목표 타입·목표치 조합이 중복되지 않는다.")
        void goalIsUnique() {
            // arrange - mongs_mission 의 uk_mission_goal 과 같은 조건이다
            List<Seed> seeds = missions();

            // act
            long distinct = seeds.stream()
                    .map(seed -> seed.actionCode() + "/" + seed.goalTypeCode() + "/" + seed.goalCount())
                    .distinct()
                    .count();

            // assert
            assertEquals(seeds.size(), distinct);
        }

        @Test
        @DisplayName("같은 액션·목표 타입이 두 주기에 걸치지 않는다.")
        void goalDoesNotSpanCycles() {
            // arrange
            Map<String, String> cycleOfPair = new HashMap<>();

            // act & assert - 일간 "밥 5번"과 주간 "밥 20번"처럼 확대판이 생기면 안 된다
            for (Seed seed : missions()) {
                String pair = seed.actionCode() + "/" + seed.goalTypeCode();
                String cycleCode = cycleOfPair.putIfAbsent(pair, seed.cycleCode());

                assertTrue(cycleCode == null || cycleCode.equals(seed.cycleCode()),
                        pair + " 가 " + cycleCode + " 와 " + seed.cycleCode() + " 에 모두 있다");
            }
        }

        @Test
        @DisplayName("일간 미션의 액션 종류가 하루 노출 개수보다 많다.")
        void dailyHasEnoughActions() {
            // arrange - 액션이 겹치지 않게 5개를 뽑으려면 액션 종류가 5개 이상이어야 한다
            long actionCount = missions().stream()
                    .filter(seed -> seed.cycleCode().equals("DAILY"))
                    .map(Seed::actionCode)
                    .distinct()
                    .count();

            // act & assert
            assertTrue(actionCount >= DAILY_PICK_COUNT, "일간 액션 종류가 " + actionCount + "개뿐이다");
        }
    }

    @Nested
    @DisplayName("미션 리워드 시드 단위 테스트")
    class MissionRewardSeed {

        @Test
        @DisplayName("모든 미션에 리워드가 하나 이상 있다.")
        void everyMissionHasReward() {
            // arrange
            Set<Long> rewarded = new HashSet<>(rewards().stream().map(Reward::missionId).toList());

            // act & assert
            for (Seed seed : missions()) {
                assertTrue(rewarded.contains(seed.missionId()), seed.missionCode() + " 에 리워드가 없다");
            }
        }

        @Test
        @DisplayName("리워드 수량은 모두 1 이상이다.")
        void rewardAmountIsPositive() {
            // act & assert
            rewards().forEach(reward -> assertTrue(reward.amount() >= 1));
        }

        @Test
        @DisplayName("인벤토리 리워드만 아이템 정보를 가진다.")
        void onlyInventoryRewardHasItem() {
            // act & assert
            for (Reward reward : rewards()) {
                if ("INVENTORY".equals(reward.rewardTypeCode())) {
                    assertNotNull(reward.rewardCode());
                    assertNotNull(reward.inventoryTypeCode());
                } else {
                    assertNull(reward.rewardCode());
                    assertNull(reward.inventoryTypeCode());
                }
            }
        }

        @Test
        @DisplayName("인벤토리 리워드는 실제 음식·간식 코드를 가리킨다.")
        void inventoryRewardPointsToRealItem() {
            // arrange - MAP 은 몽 인벤토리가 아니라 계정 도감이라 리워드로 쓸 수 없다
            Set<String> foodCodes = codesOf("mongs_food.sql", "FD");
            Set<String> snackCodes = codesOf("mongs_snack.sql", "SN");

            // act & assert
            for (Reward reward : rewards()) {
                if (!"INVENTORY".equals(reward.rewardTypeCode())) {
                    continue;
                }

                switch (reward.inventoryTypeCode()) {
                    case "FOOD" -> assertTrue(foodCodes.contains(reward.rewardCode()),
                            reward.rewardCode() + " 는 mongs_food 에 없다");
                    case "SNACK" -> assertTrue(snackCodes.contains(reward.rewardCode()),
                            reward.rewardCode() + " 는 mongs_snack 에 없다");
                    default -> fail("인벤토리 리워드에 쓸 수 없는 타입: " + reward.inventoryTypeCode());
                }
            }
        }
    }
}
