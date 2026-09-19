package com.monglife.mongs.application.mong.port.in.service;

import com.monglife.mongs.application.mong.port.annotation.PublishMongPort;
import com.monglife.mongs.application.mong.port.exception.InvalidCreateInventoryItemException;
import com.monglife.mongs.application.mong.port.exception.NotExistsMissionException;
import com.monglife.mongs.application.mong.port.exception.NotExistsMongException;
import com.monglife.mongs.application.mong.port.in.MissionUseCase;
import com.monglife.mongs.application.mong.port.in.command.ClaimMissionRewardCommand;
import com.monglife.mongs.application.mong.port.in.command.GetMissionsCommand;
import com.monglife.mongs.application.mong.port.in.command.IncreaseMissionProgressCommand;
import com.monglife.mongs.application.mong.port.out.MissionPersistencePort;
import com.monglife.mongs.application.mong.port.out.MissionReadPort;
import com.monglife.mongs.application.mong.port.out.MongEventPort;
import com.monglife.mongs.application.mong.port.out.MongPersistencePort;
import com.monglife.mongs.application.mong.port.out.vo.CreateAccountMissionVo;
import com.monglife.mongs.application.mong.port.out.vo.CreateInventoryVo;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.domain.mission.enums.MissionCycleCode;
import com.monglife.mongs.domain.mission.model.AccountMission;
import com.monglife.mongs.domain.mission.model.Mission;
import com.monglife.mongs.domain.mission.model.MissionRotation;
import com.monglife.mongs.domain.mission.model.MissionReward;
import com.monglife.mongs.domain.mong.model.Mong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Service
public class MissionService implements MissionUseCase {

    private static final Random random = new Random();

    /**
     * 서비스 기준 시간대. 서버가 어느 지역에 뜨든 사용자가 체감하는 "오늘"이 같아야 한다.
     * DeviceCacheService 의 일일 환전 상한과 같은 판단이다.
     */
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final MissionReadPort missionReadPort;

    private final MissionPersistencePort missionPersistencePort;

    private final MongPersistencePort mongPersistencePort;

    private final MongEventPort mongEventPort;

    /** 일간 미션을 하루에 몇 개 뽑을지. 마스터를 늘려도 노출 개수는 여기로 조절한다 */
    private final int dailyPickCount;

    public MissionService(
            MissionReadPort missionReadPort,
            MissionPersistencePort missionPersistencePort,
            MongPersistencePort mongPersistencePort,
            MongEventPort mongEventPort,
            @Value("${application.mission.daily-pick-count:5}") int dailyPickCount
    ) {
        this.missionReadPort = missionReadPort;
        this.missionPersistencePort = missionPersistencePort;
        this.mongPersistencePort = mongPersistencePort;
        this.mongEventPort = mongEventPort;
        this.dailyPickCount = dailyPickCount;
    }

    /**
     * 미션 목록 조회
     */
    @Override
    @Transactional
    public List<AccountMission> getMissionsUseCase(GetMissionsCommand command) {
        return this.syncAccountMissions(command.getAccountId(), LocalDate.now(SERVICE_ZONE));
    }

    /**
     * 미션 진행도 반영
     */
    @Override
    @Transactional
    public void increaseMissionProgressUseCase(IncreaseMissionProgressCommand command) {

        LocalDate today = LocalDate.now(SERVICE_ZONE);

        // 미션 목록을 한 번도 안 열어 본 사용자도 진행도가 쌓여야 한다.
        // 이미 적재돼 있으면 조회 한 번으로 끝난다.
        this.syncAccountMissions(command.getAccountId(), today);

        Map<MissionCycleCode, String> cycleKeys = this.cycleKeys(today);

        // CARE_DAY 는 전용 훅이 없다. 어떤 액션이든 들어오면 오늘 날짜로 함께 올린다.
        Set<MissionActionCode> actionCodes = new LinkedHashSet<>();
        actionCodes.add(command.getActionCode());
        actionCodes.add(MissionActionCode.CARE_DAY);

        List<AccountMission> targets = missionPersistencePort.getAccountMissionsForUpdatePort(
                command.getAccountId(), cycleKeys.values(), actionCodes);

        String careDayDetail = MissionCycleCode.DAILY.cycleKey(today);

        for (AccountMission accountMission : targets) {

            boolean isCareDay = MissionActionCode.CARE_DAY.equals(accountMission.getMission().getActionCode());

            // CARE_DAY 는 액션이 무엇이든 오늘 날짜로 센다. 그 외 액션은 자기 대상 코드를 쓰고,
            // 대상이 없는 액션(재우기 같은 하루 한 번짜리)은 날짜로 떨어진다 -
            // 그래야 "이번 달 20일 재워주기" 가 하루에 여러 번 눌러도 1로 남는다.
            String detailCode = isCareDay || command.getDetailCode() == null
                    ? careDayDetail
                    : command.getDetailCode();

            accountMission.increaseProgress(isCareDay ? null : command.getAmount(), detailCode);

            if (Boolean.TRUE.equals(accountMission.getIsProgressChange())) {
                missionPersistencePort.saveAccountMissionPort(accountMission);
            }
        }
    }

    /**
     * 미션 리워드 수령
     */
    @Override
    @Transactional
    @PublishMongPort
    public Mong claimMissionRewardUseCase(ClaimMissionRewardCommand command) {

        AccountMission accountMission = missionPersistencePort.getAccountMissionForUpdatePort(command.getAccountMissionId())
                .orElseThrow(NotExistsMissionException::new)
                .verify(command.getAccountId());

        // 지급보다 먼저 못 박는다. 상태를 먼저 CLAIMED 로 바꿔야 같은 행을 두 번 지급하는 경로가 없다.
        accountMission.claim(LocalDateTime.now());
        missionPersistencePort.saveAccountMissionPort(accountMission);

        Mong mong = mongPersistencePort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new)
                .verify(command.getAccountId());

        for (MissionReward reward : accountMission.getMission().getRewards()) {

            reward.verify();

            switch (reward.getRewardTypeCode()) {
                // 경험치는 maxStatus 로 잘리고 진화 준비 상태로 넘어갈 수 있다
                case EXP -> mong.missionReward(reward.getAmount().doubleValue());

                case PAY_POINT -> mong.increasePayPoint(reward.getAmount());

                case INVENTORY -> {
                    for (int count = 0; count < reward.getAmount(); count++) {
                        mongPersistencePort.createInventoryPort(CreateInventoryVo.builder()
                                        .mongId(mong.getMongId())
                                        .inventoryCode(reward.getRewardCode())
                                        .inventoryTypeCode(reward.getInventoryTypeCode())
                                        .build())
                                .orElseThrow(InvalidCreateInventoryItemException::new);
                    }
                }

                // 스타 포인트는 user-service 소유라 직접 못 건드린다
                case STAR_POINT -> mongEventPort.missionRewardStarPointEventPort(
                        command.getAccountId(), reward.getAmount(), accountMission.getMission().getMissionCode());
            }
        }

        // 몽 정보 동기화
        mongPersistencePort.saveMongPort(mong)
                .orElseThrow(NotExistsMongException::new);

        return mong;
    }

    /**
     * 이번 주기 사용자 미션 적재 및 조회.
     *
     * <p>주기 키가 바뀌면 그 주기의 행이 하나도 없으므로 새로 적재된다. 초기화 배치가 없는 이유다.
     * 이미 적재된 주기는 건드리지 않는다 - 그래야 그날 뽑힌 일간 5개가 재조회 때 바뀌지 않는다.
     *
     * @param accountId 계정 ID
     * @param today 서비스 기준 시간대의 오늘
     * @return 이번 주기 사용자 미션 목록
     */
    private List<AccountMission> syncAccountMissions(Long accountId, LocalDate today) {

        Map<MissionCycleCode, String> cycleKeys = this.cycleKeys(today);
        Collection<String> keys = cycleKeys.values();

        List<AccountMission> accountMissions = missionReadPort.getAccountMissionsPort(accountId, keys);

        Set<MissionCycleCode> filledCycles = new HashSet<>();
        for (AccountMission accountMission : accountMissions) {
            filledCycles.add(accountMission.getMission().getCycleCode());
        }

        List<CreateAccountMissionVo> createAccountMissionVos = new ArrayList<>();

        for (MissionCycleCode cycleCode : MissionCycleCode.values()) {

            if (filledCycles.contains(cycleCode)) {
                continue;
            }

            for (Mission mission : this.selectMissions(cycleCode, today)) {
                createAccountMissionVos.add(CreateAccountMissionVo.builder()
                        .accountId(accountId)
                        .missionId(mission.getMissionId())
                        .cycleCode(cycleCode)
                        .cycleKey(cycleKeys.get(cycleCode))
                        .build());
            }
        }

        if (createAccountMissionVos.isEmpty()) {
            return accountMissions;
        }

        missionPersistencePort.createAccountMissionsPort(createAccountMissionVos);

        return missionReadPort.getAccountMissionsPort(accountId, keys);
    }

    /**
     * 주기별 노출 미션 선정.
     *
     * <p>주간·월간은 로테이션 그룹으로 고른다 - 주기 번호로 그룹 하나를 골라 그 그룹 전부를 내보낸다.
     * 모든 사용자가 같은 묶음을 본다. 일간만 사용자별로 무작위다.
     * 뽑을 때 액션이 겹치지 않게 한다 - "밥 3번"과 "밥 10번"이 같은 날 함께 나오면
     * 다섯 칸 중 둘이 사실상 같은 미션이 된다.
     *
     * @param cycleCode 미션 주기
     * @return 적재할 미션 마스터 목록
     */
    private List<Mission> selectMissions(MissionCycleCode cycleCode, LocalDate today) {

        List<Mission> activeMissions = missionReadPort.getActiveMissionsPort(cycleCode);

        if (!MissionCycleCode.DAILY.equals(cycleCode)) {
            return MissionRotation.select(activeMissions, cycleCode, today);
        }

        Map<MissionActionCode, List<Mission>> missionsByAction = new EnumMap<>(MissionActionCode.class);
        for (Mission mission : activeMissions) {
            missionsByAction.computeIfAbsent(mission.getActionCode(), key -> new ArrayList<>()).add(mission);
        }

        List<MissionActionCode> actionCodes = new ArrayList<>(missionsByAction.keySet());
        java.util.Collections.shuffle(actionCodes, random);

        List<Mission> picked = new ArrayList<>();
        for (MissionActionCode actionCode : actionCodes) {

            if (picked.size() >= this.dailyPickCount) {
                break;
            }

            List<Mission> candidates = missionsByAction.get(actionCode);
            picked.add(candidates.get(random.nextInt(candidates.size())));
        }

        return picked;
    }

    /**
     * 오늘 기준 주기 키 묶음
     */
    private Map<MissionCycleCode, String> cycleKeys(LocalDate today) {

        Map<MissionCycleCode, String> cycleKeys = new EnumMap<>(MissionCycleCode.class);
        for (MissionCycleCode cycleCode : MissionCycleCode.values()) {
            cycleKeys.put(cycleCode, cycleCode.cycleKey(today));
        }

        return cycleKeys;
    }
}
