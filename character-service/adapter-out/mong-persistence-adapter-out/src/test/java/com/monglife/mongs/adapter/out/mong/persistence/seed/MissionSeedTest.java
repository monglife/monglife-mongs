package com.monglife.mongs.adapter.out.mong.persistence.seed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * <p>SQL 은 configs 서브모듈의 마이그레이션 파일 하나에 들어 있다(표 생성 + 시드). 기동 시 자동
 * 적재를 쓰지 않으므로 클래스패스에 없고, 저장소 루트를 거슬러 올라가 파일로 읽는다.
 */
class MissionSeedTest {

    /** (id, code, cycle, action, goalType) - 제목·설명은 한글이라 앞쪽 필드만 뽑는다 */
    private static final Pattern MISSION = Pattern.compile(
            "\\(\\s*(\\d+),\\s*'([^']+)',\\s*'([A-Z_]+)',\\s*'([A-Z_]+)',\\s*'([A-Z_]+)',");

    /**
     * 뒤쪽의 goal_count, is_active, sort_order, rotation_group.
     *
     * <p>컬럼이 늘면 여기도 늘려야 한다. 안 늘리면 뒤에서부터 밀려 goal_count 자리에
     * is_active 가 잡히고, 모든 미션의 목표치가 1 로 읽혀 중복 검사가 엉뚱하게 터진다.
     */
    private static final Pattern MISSION_TAIL = Pattern.compile(
            ",(\\d+),(\\d+),(\\d+),(\\d+),NOW\\(\\),NOW\\(\\)\\)");

    private static final Pattern REWARD = Pattern.compile(
            "\\(\\s*(\\d+),\\s*(\\d+),\\s*'([A-Z_]+)',\\s*(NULL|'[^']*'),\\s*(NULL|'[^']*'),\\s*(\\d+)\\s*\\)");

    private static final int DAILY_PICK_COUNT = 5;

    private record Seed(long missionId, String missionCode, String cycleCode, String actionCode, String goalTypeCode, int goalCount, int rotationGroup) {}

    private record Reward(long missionId, String rewardTypeCode, String rewardCode, String inventoryTypeCode, int amount) {}

    private static final String MIGRATION = "configs/migration/2026-09-17-mission.sql";

    /**
     * 마이그레이션 SQL 읽기.
     *
     * <p>테스트의 작업 디렉터리는 Gradle 서브프로젝트라 저장소 루트가 아니다. 상대 경로를
     * 고정하면(../../../) 모듈이 옮겨질 때 조용히 깨지므로, 파일이 나올 때까지 위로 올라간다.
     */
    private static String read() {

        for (Path dir = Paths.get("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            Path file = dir.resolve(MIGRATION);
            if (Files.exists(file)) {
                try {
                    return Files.readString(file, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        return fail(MIGRATION + " 을 찾지 못했다. configs 서브모듈을 받았는지 확인할 것.");
    }

    /**
     * 클래스패스 시드 읽기. 음식·간식처럼 아직 기동 시 자동 적재되는 시드용이다.
     * copyPrivate 가 리소스로 복사하므로 CI 는 테스트 전에 그 태스크를 돈다.
     */
    private static String readResource(String name) {
        try (InputStream in = MissionSeedTest.class.getClassLoader().getResourceAsStream(name)) {
            assertNotNull(in, name + " 이 클래스패스에 없다. ./gradlew copyPrivate 를 먼저 돌려야 한다.");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<Seed> missions() {

        String sql = read();
        List<Seed> seeds = new ArrayList<>();

        Matcher head = MISSION.matcher(sql);
        Matcher tail = MISSION_TAIL.matcher(sql);

        while (head.find()) {
            assertTrue(tail.find(head.end()), "미션 행의 뒤쪽 필드를 읽지 못했다: " + head.group(2));
            seeds.add(new Seed(
                    Long.parseLong(head.group(1)), head.group(2), head.group(3), head.group(4), head.group(5),
                    Integer.parseInt(tail.group(1)), Integer.parseInt(tail.group(4))));
        }

        return seeds;
    }

    private static List<Reward> rewards() {

        String sql = read();
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
        Matcher matcher = Pattern.compile("'(" + prefix + "\\d+)'").matcher(readResource(sqlName));
        while (matcher.find()) {
            codes.add(matcher.group(1));
        }

        return codes;
    }

    /** INSERT 문만 남긴다. 주석·빈 줄 차이로 드리프트 판정이 나면 안 된다 */
    private static String insertsOnly(String sql) {
        return sql.lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty() && !line.startsWith("--"))
                .reduce("", (a, b) -> a + b + "\n");
    }

    @Nested
    @DisplayName("local 시드 사본 검증")
    class LocalSeedCopy {

        /**
         * local 프로파일은 H2 create-drop 이라 기동할 때마다 표가 빈다. 마이그레이션을 매번 손으로
         * 돌릴 수 없어 configs/properties 에 사본을 둔다 - 두 벌이라 어긋날 수 있어 여기서 잡는다.
         *
         * <p>어긋나면 로컬에서만 재현되는 미션 데이터가 생겨 "내 PC 에선 되는데" 가 시작된다.
         */
        @Test
        @DisplayName("local 사본이 마이그레이션의 시드와 같다.")
        void localCopyMatchesMigration() {
            // arrange
            String migration = read();

            // act & assert
            for (String name : List.of("mongs_mission.sql", "mongs_mission_reward.sql")) {
                for (String line : insertsOnly(readResource(name)).lines().toList()) {
                    assertTrue(insertsOnly(migration).contains(line),
                            name + " 의 이 줄이 마이그레이션에 없다 - 두 벌이 어긋났다:\n  " + line);
                }
            }
        }
    }

    @Nested
    @DisplayName("미션 시드 구성 단위 테스트")
    class MissionSeed {

        @Test
        @DisplayName("일간 20개, 주간 20개, 월간 20개가 들어 있다.")
        void missionCountPerCycle() {
            // arrange
            List<Seed> seeds = missions();

            // act & assert
            assertEquals(60, seeds.size());
            assertEquals(20, seeds.stream().filter(seed -> seed.cycleCode().equals("DAILY")).count());
            assertEquals(20, seeds.stream().filter(seed -> seed.cycleCode().equals("WEEKLY")).count());
            assertEquals(20, seeds.stream().filter(seed -> seed.cycleCode().equals("MONTHLY")).count());
        }

        @Test
        @DisplayName("주간·월간은 로테이션 그룹이 10개씩 고르게 나뉘고, 일간은 그룹을 쓰지 않는다.")
        void rotationGroupBalance() {
            // arrange
            List<Seed> seeds = missions();

            // act & assert
            // 한 주기에 사용자가 보는 개수가 그룹 크기다. 한쪽이 크면 주차마다 미션 수가 널뛴다.
            for (String cycle : List.of("WEEKLY", "MONTHLY")) {
                for (int group = 0; group <= 1; group++) {
                    final int g = group;
                    assertEquals(10,
                            seeds.stream().filter(s -> s.cycleCode().equals(cycle) && s.rotationGroup() == g).count(),
                            cycle + " 그룹 " + group + " 개수");
                }
            }

            // 일간은 사용자별 무작위라 그룹으로 묶으면 뽑기 후보가 좁아진다
            assertTrue(seeds.stream().filter(s -> s.cycleCode().equals("DAILY")).allMatch(s -> s.rotationGroup() == 0),
                    "일간은 전부 그룹 0 이어야 한다");
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
