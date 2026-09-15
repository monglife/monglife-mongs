package com.monglife.mongs.domain.mong.model;

import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import com.monglife.mongs.domain.mong.exception.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Getter
@ToString
public class Mong {

    private static final Random random = new Random();
    // 최대 레벨
    private static final int MAX_LEVEL = 3;
    // 최대 배변 수
    private static final int MAX_POOP_COUNT = 4;
    // 쓰다 듬기 대기 시간
    private static final long STROKE_EXPIRED_SECONDS = 300L;
    // 랜덤 뽑기 시 소비 페이 포인트
    private static final int RANDOM_DRAW_PAY_POINT = 100;

    private final Long mongId;

    private final Long accountId;

    private final String name;

    private String mongCode;

    private String mongName;

    private MongStatusCode statusCode;

    private MongStateCode stateCode;

    private Integer level;

    private Double maxStatus;

    private final LocalTime sleepAt;

    private final LocalTime wakeupAt;

    private Integer payPoint;

    private Boolean isSleep;

    private Double strength;

    private Double satiety;

    private Double healthy;

    private Double fatigue;

    private Double exp;

    private Double weight;

    private Double evolutionReward;

    private Double evolutionPenalty;

    private Integer strokeCount;

    private Integer trainingCount;

    private Integer poopCount;

    private Integer randomDrawTicketCount;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    private Boolean isMongStateChange;

    private Boolean isMongStatusCodeChange;

    @Builder
    public Mong(Long mongId, Long accountId, String name, String mongCode, String mongName, MongStatusCode statusCode, MongStateCode stateCode, Integer level, Double maxStatus, LocalTime sleepAt, LocalTime wakeupAt, Integer payPoint, Boolean isSleep, Double strength, Double satiety, Double healthy, Double fatigue, Double exp, Double weight, Double evolutionReward, Double evolutionPenalty, Integer strokeCount, Integer trainingCount, Integer poopCount, Integer randomDrawTicketCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.mongId = mongId;
        this.accountId = accountId;
        this.name = name;
        this.mongCode = mongCode;
        this.mongName = mongName;
        this.statusCode = statusCode;
        this.stateCode = stateCode;
        this.level = level;
        this.maxStatus = maxStatus;
        this.sleepAt = sleepAt;
        this.wakeupAt = wakeupAt;
        this.payPoint = payPoint;
        this.isSleep = isSleep;
        this.strength = strength;
        this.satiety = satiety;
        this.healthy = healthy;
        this.fatigue = fatigue;
        this.exp = exp;
        this.weight = weight;
        this.evolutionReward = evolutionReward;
        this.evolutionPenalty = evolutionPenalty;
        this.strokeCount = strokeCount;
        this.trainingCount = trainingCount;
        this.poopCount = poopCount;
        this.randomDrawTicketCount = randomDrawTicketCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isMongStateChange = false;
        this.isMongStatusCodeChange = false;
    }

    /**
     * 몽 권한 확인
     * @param accountId 계정 ID
     * @return 몽 도메인 객체
     */
    public Mong verify(Long accountId) {

        if (!this.accountId.equals(accountId)) {
            throw new ForbiddenMongException();
        }

        return this;
    }

    /**
     * 몽 사망
     */
    public void dead() {

        if (MongStateCode.DEAD.equals(this.stateCode) ||
            MongStateCode.GRADUATE_READY.equals(this.stateCode)
        ) throw new InvalidMongStateException();

        this.updateStateCode(MongStateCode.DEAD);
    }

    /**
     * 쓰다 듬기
     */
    public void stroke() {

        if (MongStateCode.DEAD.equals(this.stateCode) ||
            MongStateCode.GRADUATE_READY.equals(this.stateCode) ||
            this.level == 0 ||
            this.isSleep
        ) throw new InvalidMongStateException();

        this.exp = Math.max(0, Math.min(this.exp + 5D, this.maxStatus));
        this.strokeCount = this.strokeCount + 1;

        // 몽 상태 코드 동기화
        this.syncMongStateCode();
    }

    /**
     * 수면
     */
    public void sleep() {

        if (MongStateCode.DEAD.equals(this.stateCode) ||
            MongStateCode.GRADUATE_READY.equals(this.stateCode) ||
            this.level == 0 ||
            this.isSleep
        ) throw new InvalidMongStateException();

        this.isSleep = true;
    }

    /**
     * 기상
     */
    public void wakeup() {

        if (MongStateCode.DEAD.equals(this.stateCode) ||
            MongStateCode.GRADUATE_READY.equals(this.stateCode) ||
            this.level == 0 ||
            !this.isSleep
        ) throw new InvalidMongStateException();

        this.isSleep = false;
    }

    /**
     * 배변 처리
     */
    public void poopClean() {

        if (MongStateCode.DEAD.equals(this.stateCode) ||
            MongStateCode.GRADUATE_READY.equals(this.stateCode) ||
            this.level == 0 ||
            this.isSleep
        ) throw new InvalidMongStateException();

        this.exp = Math.max(0, Math.min(this.exp + this.poopCount * 2.0, this.maxStatus));
        this.poopCount = 0;

        // 몽 상태 코드 동기화
        this.syncMongStateCode();
    }

    /**
     * 진화 준비
     */
    public void evolutionReady() {
        this.updateStateCode(MongStateCode.EVOLUTION_READY);
    }

    /**
     * 진화 스코어 계산
     */
    private Double getEvolutionScore() {
        double evolutionScore = 50D;

        evolutionScore = Math.max(0, evolutionScore + 1.00 * this.evolutionReward);
        evolutionScore = Math.max(0, evolutionScore - 1.00 * this.evolutionPenalty);
        evolutionScore = Math.max(0, evolutionScore + 2.00 * this.strokeCount);
        evolutionScore = Math.max(0, evolutionScore + 5.00 * this.trainingCount);

        return evolutionScore;
    }

    /**
     * 진화
     * @param mongTypes 진화 가능한 몽 타입 목록
     */
    public Double evolution(List<MongType> mongTypes, List<MongEvolutionHistory> mongEvolutionHistories) {

        if (MongStateCode.DEAD.equals(this.stateCode) ||
            MongStateCode.GRADUATE_READY.equals(this.stateCode) ||
            !MongStateCode.EVOLUTION_READY.equals(this.stateCode)
        ) throw new InvalidMongStateException();


        List<String> mongEvolutionHistoryMongCodes = mongEvolutionHistories.stream()
                .map(MongEvolutionHistory::getMongCode)
                .toList();

        // 진화 스코어 기준 필터링
        double evolutionScore = this.getEvolutionScore();
        List<MongType> sortedMongTypes = mongTypes.stream()
                .peek(mongType -> {
                    if (mongEvolutionHistoryMongCodes.contains(mongType.getMongCode())) {
                        // 이미 컬렉션 보유 중인 몽인 경우 진화 스코어 65% 감소 패치
                        mongType.fetchEvolutionScore(0.70);
                    }
                })
                .filter(mongType -> mongType.getEvolutionScore() <= evolutionScore)
                .sorted((o1, o2) -> o2.getEvolutionScore().compareTo(o1.getEvolutionScore()))
                .toList();

        // 진화 가능한 몽 타입이 없는 경우 예외
        if (sortedMongTypes.isEmpty()) {
            throw new InvalidEvolutionException();
        }

        // 다음 몽 타입 지정
        MongType mongType;
        double evolutionReward;

        if (this.level == 0) {
            // 다음 몽 타입 지정
            mongType = sortedMongTypes.get((int) (random.nextLong(0, Long.MAX_VALUE) % (long) mongTypes.size()));
            evolutionReward = 0;
        } else {
            // 다음 몽 타입 지정
            mongType = sortedMongTypes.get(0);
            // 진화 점수 계산
            evolutionReward = Math.max(0, Math.min(evolutionScore - 100D, 25D));
        }

        // 지수 수치 -> 지수 비율 퍼센트 변환
        double strengthRatio = this.strength / this.maxStatus * 100;
        double satietyRatio  = this.satiety  / this.maxStatus * 100;
        double healthyRatio  = this.healthy  / this.maxStatus * 100;
        double fatigueRatio  = this.fatigue  / this.maxStatus * 100;

        // 진화 리워드 점수 갱신
        this.evolutionReward  = evolutionReward;
        this.evolutionPenalty = 0D;
        this.mongCode         = mongType.getMongCode();
        this.mongName         = mongType.getMongName();
        this.level            = mongType.getLevel();
        this.maxStatus        = mongType.getMaxStatus();

        this.strength = Math.max(0, Math.min(mongType.getMaxStatus() * strengthRatio / 100, this.maxStatus));
        this.satiety  = Math.max(0, Math.min(mongType.getMaxStatus() * satietyRatio  / 100, this.maxStatus));
        this.healthy  = Math.max(0, Math.min(mongType.getMaxStatus() * healthyRatio  / 100, this.maxStatus));
        this.fatigue  = Math.max(0, Math.min(mongType.getMaxStatus() * fatigueRatio  / 100, this.maxStatus));
        this.exp = 0D;

        this.updateStateCode(MongStateCode.NORMAL);

        return evolutionScore;
    }

    /**
     * 몽 졸업
     */
    public void graduate() {

        if (MongStateCode.DEAD.equals(this.stateCode) ||
            !MongStateCode.GRADUATE_READY.equals(this.stateCode) ||
            this.level == 0
        ) throw new InvalidMongStateException();

        this.updateStateCode(MongStateCode.GRADUATE);
        this.updateStatusCode(MongStatusCode.NORMAL);
    }

    /**
     * 몽 페이 포인트 증가
     * @param payPoint 증가할 페이 포인트
     */
    public void increasePayPoint(Integer payPoint) {
        this.payPoint = Math.max(0, this.payPoint + payPoint);
    }

    /**
     * 몽 지수 1 Cycle 증가
     */
    public void cycleIncreaseStatus() {
        if (!MongStateCode.DEAD.equals(this.stateCode)) {
            double addStrength = 0.015 * this.maxStatus;
            double addHealthy  = 0.020  * this.maxStatus;
            double addFatigue  = 0.060  * this.maxStatus;

            this.strength = Math.max(0, Math.min(this.strength + addStrength, this.maxStatus));
            this.healthy  = Math.max(0, Math.min(this.healthy + addHealthy, this.maxStatus));
            this.fatigue  = Math.max(0, Math.min(this.fatigue + addFatigue, this.maxStatus));

            // 몽 지수 코드 동기화
            this.syncMongStatusCode();
        }
    }

    /**
     * 몽 지수 1 Cycle 감소
     */
    public void cycleDecreaseStatus() {
        if (!MongStateCode.DEAD.equals(this.stateCode)) {
            double subWeight   = 0.025 * this.weight;
            double subStrength = 0.035 * this.maxStatus;
            double subSatiety  = 0.025 * this.maxStatus;
            double subHealthy  = 0.025 * this.maxStatus;
            double subFatigue  = 0.015 * this.maxStatus;

            this.weight   = Math.max(0, this.weight - subWeight);
            this.strength = Math.max(0, Math.min(this.strength - subStrength, this.maxStatus));
            this.satiety  = Math.max(0, Math.min(this.satiety - subSatiety, this.maxStatus));
            this.healthy  = Math.max(0, Math.min(this.healthy - subHealthy, this.maxStatus));
            this.fatigue  = Math.max(0, Math.min(this.fatigue - subFatigue, this.maxStatus));

            // 몽 지수 코드 동기화
            this.syncMongStatusCode();
        }
    }

    /**
     * 몽 배변 수 1 Cycle 증가
     */
    public void cycleIncreasePoopCount() {
        if (!MongStateCode.DEAD.equals(this.stateCode)) {
            int addPoopCount = 1;

            // 최대 배변 수를 초과한 경우 진화 패널티 증가
            if (this.poopCount + addPoopCount >= MAX_POOP_COUNT) {
                this.evolutionPenalty = this.evolutionPenalty + 0.1;
            }

            this.poopCount = Math.min(this.poopCount + addPoopCount, MAX_POOP_COUNT);
        }
    }

    /**
     * 훈련
     * @param trainingType 훈련 타입 도메인 객체
     */
    public void training(TrainingType trainingType) {

        if (MongStateCode.DEAD.equals(this.stateCode)) {
            throw new InvalidMongStateException();
        }

        this.exp           = Math.max(0, Math.min(this.exp + trainingType.getExp(), this.maxStatus));
        this.strength      = Math.max(0, Math.min(this.strength + trainingType.getStrength(), this.maxStatus));
        this.satiety       = Math.max(0, Math.min(this.satiety + trainingType.getSatiety(), this.maxStatus));
        this.fatigue       = Math.max(0, Math.min(this.fatigue + trainingType.getFatigue(), this.maxStatus));
        this.weight        = Math.max(0, this.weight + trainingType.getWeight());
        this.trainingCount = Math.max(0, this.trainingCount + 1);

        // 몽 상태 코드 동기화
        this.syncMongStateCode();
        // 몽 지수 코드 동기화
        this.syncMongStatusCode();
    }

    /**
     * 훈련 목표치 달성 완료
     * @param trainingType 훈련 타입 도메인 객체
     */
    public void trainingWithReward(TrainingType trainingType) {

        if (MongStateCode.DEAD.equals(this.stateCode)) {
            throw new InvalidMongStateException();
        }

        // 훈련 완료
        this.training(trainingType);
        // 스코어 달성 시 페이 포인트 증가
        this.payPoint = Math.max(0, this.payPoint + trainingType.getPayPoint());
    }

    /**
     * 음식 구매 후 섭취
     * @param food 음식 도메인 객체
     */
    public void feedWithBuy(Food food) {

        if (MongStateCode.DEAD.equals(this.stateCode) ||
            this.level == 0 ||
            this.isSleep
        ) throw new InvalidMongStateException();

        // 구매할 수 없는 경우 예외 발생
        if (!food.getIsCanBuy()) {
            throw new InvalidFeedFoodException();
        }

        // 충분한 페이 포인트 없는 경우 예외 발생
        if (this.payPoint < food.getPrice()) {
            throw new NotEnoughPayPointException();
        }

        this.payPoint = Math.max(0, this.payPoint - food.getPrice());
        this.feed(food);
    }

    /**
     * 간식 구매 후 섭취
     * @param snack 간식 도메인 객체
     */
    public void feedWithBuy(Snack snack) {

        if (MongStateCode.DEAD.equals(this.stateCode) ||
            this.level == 0 ||
            this.isSleep
        ) throw new InvalidMongStateException();

        // 구매할 수 없는 경우 예외 발생
        if (!snack.getIsCanBuy()) {
            throw new InvalidFeedSnackException();
        }

        // 페이 포인트가 충분하지 않은 경우 예외 발생
        if (this.payPoint < snack.getPrice()) {
            throw new NotEnoughPayPointException();
        }

        this.payPoint = Math.max(0, this.payPoint - snack.getPrice());
        this.feed(snack);
    }

    /**
     * 음식 섭취
     * @param food 음식 도메인 객체
     */
    public void feed(Food food) {

        if (MongStateCode.DEAD.equals(this.stateCode) ||
            this.level == 0 ||
            this.isSleep
        ) throw new InvalidMongStateException();

        this.strength = Math.max(0, Math.min(this.strength + food.getStrength(), this.maxStatus));
        this.satiety  = Math.max(0, Math.min(this.satiety  + food.getSatiety(), this.maxStatus));
        this.healthy  = Math.max(0, Math.min(this.healthy  + food.getHealthy(), this.maxStatus));
        this.fatigue  = Math.max(0, Math.min(this.fatigue  + food.getFatigue(), this.maxStatus));
        this.weight   = Math.max(0, Math.min(this.weight   + food.getWeight(), this.maxStatus));

        // 몽 지수 코드 동기화
        this.syncMongStatusCode();
    }

    /**
     * 간식 섭취
     * @param snack 간식 도메인 객체
     */
    public void feed(Snack snack) {

        if (MongStateCode.DEAD.equals(this.stateCode) ||
            this.level == 0 ||
            this.isSleep
        ) throw new InvalidMongStateException();

        this.strength = Math.max(0, Math.min(this.strength + snack.getStrength(), this.maxStatus));
        this.satiety  = Math.max(0, Math.min(this.satiety  + snack.getSatiety(), this.maxStatus));
        this.healthy  = Math.max(0, Math.min(this.healthy  + snack.getHealthy(), this.maxStatus));
        this.fatigue  = Math.max(0, Math.min(this.fatigue  + snack.getFatigue(), this.maxStatus));
        this.weight   = Math.max(0, Math.min(this.weight   + snack.getWeight(), this.maxStatus));

        // 몽 지수 코드 동기화
        this.syncMongStatusCode();
    }

    /**
     * 랜덤 뽑기 티켓 구매
     */
    public void buyRandomDrawTicket() {

        if (MongStateCode.DEAD.equals(this.stateCode)) {
            throw new InvalidMongStateException();
        }

        // 랜덤 뽑기 티켓 구매 불가능 경우
        if (this.payPoint < RANDOM_DRAW_PAY_POINT) {
            throw new NotEnoughPayPointException();
        }
        // 뽑기 횟수 페이 포인트 구매
        this.payPoint = Math.max(0, this.payPoint - RANDOM_DRAW_PAY_POINT);
        this.randomDrawTicketCount = this.randomDrawTicketCount + 1;
    }

    /**
     * 랜덤 뽑기 티켓 감소
     */
    public void decreaseRandomDrawTicketCount() {

        if (MongStateCode.DEAD.equals(this.stateCode)) {
            throw new InvalidMongStateException();
        }

        // 랜덤 뽑기 가능 횟수가 없는 경우
        if (this.randomDrawTicketCount == 0) {
            throw new NotEnoughRandomDrawTicketException();
        }

        this.randomDrawTicketCount = Math.max(0, this.randomDrawTicketCount - 1);
    }

    /**
     * 매치 배팅
     * @param payPoint 배팅 페이 포인트
     */
    public void matchBetting(Integer payPoint) {

        if (MongStateCode.DEAD.equals(this.stateCode)) {
            throw new InvalidMongStateException();
        }

        if (this.payPoint < payPoint) {
            throw new NotEnoughPayPointException();
        }

        // 페이 포인트 감소
        this.payPoint = Math.max(0, this.payPoint - payPoint);
    }

    /**
     * 매치 배팅 취소
     * @param payPoint 배팅 페이 포인트
     */
    public void matchBettingCancel(Integer payPoint) {
        // 페이 포인트 증가
        this.payPoint = Math.max(0, this.payPoint + payPoint);
    }

    /**
     * 매치 보상
     * @param payPoint 보상 페이 포인트
     * @param exp 보상 경험치
     */
    public void matchReward(Integer payPoint, Double exp) {
        // 페이 포인트 증가
        this.payPoint = this.payPoint + payPoint;
        this.exp = Math.max(0, Math.min(this.exp + exp, this.maxStatus));

        // 몽 상태 코드 동기화
        this.syncMongStateCode();
    }

    /**
     * 몽 상태 코드 동기화
     */
    private void syncMongStateCode() {

        // 경험치 기준 상태 검증 및 변경
        if (this.exp >= this.maxStatus) {
            this.updateStateCode(this.level == MAX_LEVEL ? MongStateCode.GRADUATE_READY : MongStateCode.EVOLUTION_READY);
        }
    }

    /**
     * 몽 지수 코드 동기화
     */
    private void syncMongStatusCode() {

        double satietyRatio = this.satiety / this.maxStatus * 100;
        double healthyRatio = this.healthy / this.maxStatus * 100;
        double fatigueRatio = this.fatigue / this.maxStatus * 100;

        if (healthyRatio < 10) {
            this.updateStatusCode(MongStatusCode.SICK);
        } else if (fatigueRatio < 10) {
            this.updateStatusCode(MongStatusCode.SOMNOLENCE);
        } else if (satietyRatio < 10) {
            this.updateStatusCode(MongStatusCode.HUNGRY);
        } else {
            this.updateStatusCode(MongStatusCode.NORMAL);
        }
    }

    /**
     * 상태 코드 수정
     * @param stateCode 상태 코드
     */
    private void updateStateCode(MongStateCode stateCode) {

        if (MongStateCode.DEAD.equals(this.stateCode)) {
            throw new InvalidMongStateException();
        }

        if (this.stateCode != stateCode) {
            this.stateCode = stateCode;
            this.isMongStateChange = true;
        }
    }

    /**
     * 지수 코드 수정
     * @param statusCode 지수 코드
     */
    private void updateStatusCode(MongStatusCode statusCode) {

        if (MongStateCode.DEAD.equals(this.stateCode)) {
            throw new InvalidMongStateException();
        }

        if (this.statusCode != statusCode) {
            this.statusCode = statusCode;
            this.isMongStatusCodeChange = true;
        }
    }

    /**
     * 관리자 지수 수정. null 인 항목은 건드리지 않고, 값은 0 ~ maxStatus 로 잘라 넣는다.
     * DEAD 상태에서는 상태·지수 코드 동기화를 건너뛴다(동기화가 DEAD 를 거부한다).
     */
    public void adminUpdateStatus(Double weight, Double strength, Double satiety, Double healthy, Double fatigue, Double exp, Integer payPoint, Integer poopCount, Integer randomDrawTicketCount) {

        if (weight   != null) this.weight   = Math.max(0, weight);
        if (strength != null) this.strength = clampStatus(strength);
        if (satiety  != null) this.satiety  = clampStatus(satiety);
        if (healthy  != null) this.healthy  = clampStatus(healthy);
        if (fatigue  != null) this.fatigue  = clampStatus(fatigue);
        if (exp      != null) this.exp      = clampStatus(exp);
        if (payPoint != null) this.payPoint = Math.max(0, payPoint);
        if (poopCount != null) this.poopCount = Math.max(0, Math.min(poopCount, MAX_POOP_COUNT));
        if (randomDrawTicketCount != null) this.randomDrawTicketCount = Math.max(0, randomDrawTicketCount);

        if (!MongStateCode.DEAD.equals(this.stateCode)) {
            // 몽 상태 코드 동기화
            this.syncMongStateCode();
            // 몽 지수 코드 동기화
            this.syncMongStatusCode();
        }
    }

    /**
     * 관리자 상태 코드 강제 변경. DEAD 복구처럼 도메인 규칙을 우회해야 하는 운영 조치용.
     * 지수 코드는 NORMAL 로 되돌린 뒤 현재 지수 기준으로 다시 맞춘다.
     */
    public void adminUpdateStateCode(MongStateCode stateCode) {

        if (this.stateCode != stateCode) {
            this.stateCode = stateCode;
            this.isMongStateChange = true;
        }

        if (!MongStateCode.DEAD.equals(this.stateCode)) {
            this.syncMongStatusCode();
        }
    }

    private double clampStatus(double value) {
        return Math.max(0, Math.min(value, this.maxStatus));
    }

    /**
     * 쓰다 듬기 대기 시간 조회
     * @return 쓰다 듬기 대기 시간
     */
    public static Long getStrokeExpirationSeconds() {
        return STROKE_EXPIRED_SECONDS;
    }

    /**
     * 최대 배변 수 조회
     */
    public static Integer getMaxPoopCount() {
        return MAX_POOP_COUNT;
    }
}
