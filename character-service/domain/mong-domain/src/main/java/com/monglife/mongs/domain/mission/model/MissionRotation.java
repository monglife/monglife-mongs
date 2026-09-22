package com.monglife.mongs.domain.mission.model;

import com.monglife.mongs.domain.mission.enums.MissionCycleCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * 주간·월간 미션 로테이션.
 *
 * <p>활성 미션을 {@code rotationGroup} 으로 묶고, 주기 번호로 그룹 하나를 골라 그 그룹만 내보낸다.
 * 주차가 바뀌면 다음 그룹으로 넘어가고 마지막 그룹 다음은 다시 첫 그룹이다 - 날짜를 손으로
 * 채워 넣지 않아도 무한히 돈다.
 *
 * <p>모든 사용자가 같은 주기에 같은 묶음을 본다. 일간처럼 사용자별로 뽑지 않는다.
 */
public final class MissionRotation {

    private MissionRotation() {
    }

    /**
     * 이번 주기에 내보낼 미션.
     *
     * @param activeMissions 그 주기의 활성 미션 전부
     * @param cycleCode      주기
     * @param date           기준 날짜 (서비스 기준 시간대의 오늘)
     * @return 이번 주기 그룹의 미션. 활성 미션이 없으면 빈 목록
     */
    public static List<Mission> select(List<Mission> activeMissions, MissionCycleCode cycleCode, LocalDate date) {

        Integer group = currentGroup(activeMissions, cycleCode, date);

        if (group == null) {
            return Collections.emptyList();
        }

        List<Mission> selected = new ArrayList<>();
        for (Mission mission : activeMissions) {
            if (group.equals(mission.getRotationGroup())) {
                selected.add(mission);
            }
        }

        return selected;
    }

    /**
     * 이번 주기에 당첨된 그룹 번호.
     *
     * <p>그룹 번호로 나머지 연산을 하지 않는다. 존재하는 그룹만 정렬해 그 목록의 인덱스를 고른다 -
     * 그래야 0, 2, 5 처럼 번호가 듬성듬성해도 빈 주기 없이 순환한다. 번호로 나누면 그룹 1이
     * 비어 있는 주에 미션이 0개가 된다.
     *
     * @return 그룹 번호. 활성 미션이 없으면 {@code null}
     */
    public static Integer currentGroup(List<Mission> activeMissions, MissionCycleCode cycleCode, LocalDate date) {

        if (activeMissions == null || activeMissions.isEmpty()) {
            return null;
        }

        List<Integer> groups = new ArrayList<>(new TreeSet<>(activeMissions.stream()
                .map(Mission::getRotationGroup)
                .toList()));

        // periodIndex 는 1970년 기준이라 항상 0 이상이지만, 음수가 들어와도 안전하게 접는다.
        long index = Math.floorMod(cycleCode.periodIndex(date), groups.size());

        return groups.get((int) index);
    }

    /**
     * 이 미션이 지금 게시 중인가.
     *
     * <p>게시 중이면 사용자 화면에 떠 있고 진행도가 쌓이는 중이라 수정·삭제를 막는다.
     *
     * <p>일간은 로테이션을 쓰지 않는다. 활성이면 매일 뽑기 후보라 항상 게시 중으로 본다.
     */
    public static boolean isPublished(Mission mission, List<Mission> activeMissions, LocalDate date) {

        if (mission == null || !Boolean.TRUE.equals(mission.getIsActive())) {
            return false;
        }

        if (MissionCycleCode.DAILY.equals(mission.getCycleCode())) {
            return true;
        }

        return mission.getRotationGroup().equals(currentGroup(activeMissions, mission.getCycleCode(), date));
    }
}
