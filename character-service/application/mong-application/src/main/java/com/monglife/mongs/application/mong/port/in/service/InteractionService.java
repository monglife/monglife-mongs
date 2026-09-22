package com.monglife.mongs.application.mong.port.in.service;

import com.monglife.core.vo.page.PageResult;
import com.monglife.mongs.application.mong.port.annotation.CheckMongDead;
import com.monglife.mongs.application.mong.port.annotation.MissionProgress;
import com.monglife.mongs.application.mong.port.annotation.PublishMongPort;
import com.monglife.mongs.application.mong.port.exception.*;
import com.monglife.mongs.application.mong.port.in.InteractionUseCase;
import com.monglife.mongs.application.mong.port.in.MissionUseCase;
import com.monglife.mongs.application.mong.port.in.command.*;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.application.mong.port.out.MongEventPort;
import com.monglife.mongs.application.mong.port.out.MongPersistencePort;
import com.monglife.mongs.application.mong.port.out.MongReadPort;
import com.monglife.mongs.application.mong.port.out.vo.CreateInventoryVo;
import com.monglife.mongs.domain.mong.exception.ForbiddenInventoryItemException;
import com.monglife.mongs.domain.mong.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class InteractionService implements InteractionUseCase {

    private static final Random random = new Random();

    private final MongPersistencePort mongPersistencePort;

    private final MongReadPort mongReadPort;

    private final MongEventPort mongEventPort;

    private final MissionUseCase missionUseCase;

    /**
     * 음식 목록 조회
     */
    @Override
    @Transactional
    public List<Food> getFoodsUseCase(GetFoodsCommand command) {

        Mong mong = mongReadPort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new)
                .verify(command.getAccountId());

        return mongReadPort.getFoodsPort(mong.getMongId());
    }

    /**
     * 간식 목록 조회
     */
    @Override
    @Transactional
    public List<Snack> getSnacksUseCase(GetSnacksCommand command) {

        Mong mong = mongReadPort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new)
                .verify(command.getAccountId());

        return mongReadPort.getSnacksPort(mong.getMongId());
    }

    /**
     * 음식 섭취
     */
    @Override
    @Transactional
    @CheckMongDead
    @PublishMongPort
    @MissionProgress(value = MissionActionCode.FEED_FOOD, detailField = "foodCode")
    public Mong feedFoodUseCase(FeedFoodCommand command) {

        Mong mong = mongPersistencePort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new)
                .verify(command.getAccountId());

        // 음식 조회
        Food food = mongReadPort.getFoodPort(command.getFoodCode(), mong.getMongId())
                .orElseThrow(NotExistsFoodException::new);

        int payPointBeforeFeed = mong.getPayPoint();

        // 음식 섭취
        mong.feedWithBuy(food);

        // 소비 페이 포인트 누적 미션. 가격을 다시 계산하지 않고 실제 차감액을 쓴다
        this.accumulatePayPointSpend(command.getAccountId(), payPointBeforeFeed - mong.getPayPoint());

        // 몽 정보 동기화
        mongPersistencePort.saveMongPort(mong)
                .orElseThrow(NotExistsMongException::new);

        // 몽 섭취 이력 등록
        mongPersistencePort.createMongFeedFoodHistoryPort(mong.getMongId(), food.getFoodCode())
                .orElseThrow(InvalidCreateMongFeedHistoryException::new);

        return mong;
    }

    /**
     * 간식 섭취
     */
    @Override
    @Transactional
    @CheckMongDead
    @PublishMongPort
    @MissionProgress(value = MissionActionCode.FEED_SNACK, detailField = "snackCode")
    public Mong feedSnackUseCase(FeedSnackCommand command) {

        Mong mong = mongPersistencePort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new)
                .verify(command.getAccountId());

        // 간식 조회
        Snack snack = mongReadPort.getSnackPort(command.getSnackCode(), mong.getMongId())
                .orElseThrow(NotExistsSnackException::new);

        int payPointBeforeFeed = mong.getPayPoint();

        // 간식 섭취
        mong.feedWithBuy(snack);

        // 소비 페이 포인트 누적 미션
        this.accumulatePayPointSpend(command.getAccountId(), payPointBeforeFeed - mong.getPayPoint());

        // 몽 정보 동기화
        mongPersistencePort.saveMongPort(mong)
                .orElseThrow(NotExistsMongException::new);

        // 몽 섭취 이력 등록
        mongPersistencePort.createMongFeedSnackHistoryPort(mong.getMongId(), snack.getSnackCode())
                .orElseThrow(InvalidCreateMongFeedHistoryException::new);

        return mong;
    }

    /**
     * 인벤 아이템 목록 조회
     */
    @Override
    @Transactional
    public PageResult<Inventory> getInventoriesUseCase(GetInventoriesCommand command) {

        Mong mong = mongReadPort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new)
                .verify(command.getAccountId());

        return mongReadPort.getInventoriesPort(mong.getMongId(), command.getPage(), command.getSize());
    }

    /**
     * 인벤 소비성 아이템 사용
     */
    @Override
    @Transactional
    @CheckMongDead
    @PublishMongPort
    public Mong useInventoryUseCase(UseInventoryCommand command) {

        Mong mong = mongPersistencePort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new)
                .verify(command.getAccountId());

        Inventory inventory = mongPersistencePort.getInventoryPort(command.getInventoryId())
                .orElseThrow(NotExistsInventoryItemException::new)
                .verify(command.getMongId());

        // 인벤 아이템 소유자 체크
        if (!mong.getMongId().equals(inventory.getMongId())) {
            throw new ForbiddenInventoryItemException();
        }

        switch (inventory.getInventoryTypeCode()) {
            // 음식 섭취
            case FOOD -> mong.feed(mongReadPort.getFoodPort(inventory.getInventoryCode(), mong.getMongId())
                    .orElseThrow(NotExistsFoodException::new));
            // 간식 섭취
            case SNACK -> mong.feed(mongReadPort.getSnackPort(inventory.getInventoryCode(), mong.getMongId())
                    .orElseThrow(NotExistsSnackException::new));
            // 이외의 경우 예외
            default -> throw new InvalidUseInventoryItemException();
        }

        // 몽 정보 동기화
        mongPersistencePort.saveMongPort(mong)
                .orElseThrow(NotExistsMongException::new);

        // 인벤 아이템 삭제
        mongPersistencePort.deleteInventoryPort(inventory.getInventoryId())
                .orElseThrow(InvalidDeleteInventoryItemException::new);

        // 아이템 코드가 커맨드에도 반환값에도 없어 애노테이션으로는 못 넘긴다
        missionUseCase.increaseMissionProgressUseCase(IncreaseMissionProgressCommand.builder()
                .accountId(command.getAccountId())
                .actionCode(MissionActionCode.USE_INVENTORY)
                .detailCode(inventory.getInventoryCode())
                .build());

        return mong;
    }

    /**
     * 랜덤 뽑기 티켓 구매
     */
    @Override
    @Transactional
    @MissionProgress(MissionActionCode.BUY_RANDOM_DRAW_TICKET)
    public Mong buyRandomDrawTicketUseCase(BuyRandomDrawTicketCommand command) {

        Mong mong = mongPersistencePort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new)
                .verify(command.getAccountId());

        int payPointBeforeBuy = mong.getPayPoint();

        // 랜덤 뽑기 티켓 구매
        mong.buyRandomDrawTicket();

        // 소비 페이 포인트 누적 미션
        this.accumulatePayPointSpend(command.getAccountId(), payPointBeforeBuy - mong.getPayPoint());

        // 몽 정보 동기화
        mongPersistencePort.saveMongPort(mong)
                .orElseThrow(NotExistsMongException::new);

        return mong;
    }

    /**
     * 랜덤 뽑기
     */
    @Override
    @Transactional
    @MissionProgress(value = MissionActionCode.RANDOM_DRAW, detailField = "randomDrawCode")
    public RandomDraw randomDrawUseCase(RandomDrawCommand command) {

        Mong mong = mongPersistencePort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new)
                .verify(command.getAccountId());

        // 랜덤 뽑기 횟수 차감
        mong.decreaseRandomDrawTicketCount();

        // 몽 정보 동기화
        mongPersistencePort.saveMongPort(mong)
                .orElseThrow(NotExistsMongException::new);

        // 랜덤 뽑기 아이템 목록 조회
        List<RandomDraw> randomDraws = mongReadPort.getRandomDrawsPort(command.getAccountId());

        // 랜덤 뽑기 아이템 목록이 없는 경우 예외
        if (randomDraws.isEmpty()) {
            throw new NotExistsRandomDrawItemsException();
        }

        // 코드 값 랜덤 선정
        int randomDrawItemsIndex = random.nextInt(0, randomDraws.size());

        // 랜덤 뽑기 아이템 선정
        RandomDraw randomDraw = randomDraws.get(randomDrawItemsIndex);

        // 랜덤 뽑기 이력 등록
        mongPersistencePort.createRandomDrawHistoryPort(command.getAccountId(), randomDraw);

        switch (randomDraw.getInventoryTypeCode()) {
            // 맵인 경우 컬렉션 맵 등록
            case MAP -> mongEventPort.randomDrawMapEventPort(command.getAccountId(), randomDraw.getRandomDrawCode());
            // 음식, 간식인 경우 인벤 등록
            case FOOD, SNACK ->
                    mongPersistencePort.createInventoryPort(CreateInventoryVo.builder()
                        .mongId(mong.getMongId())
                        .inventoryCode(randomDraw.getRandomDrawCode())
                        .inventoryTypeCode(randomDraw.getInventoryTypeCode())
                        .build())
                        .orElseThrow(InvalidCreateInventoryItemException::new);
        }

        return randomDraw;
    }

    /**
     * 소비 페이 포인트 누적 미션 반영. 0 이하면 부를 필요가 없다
     */
    private void accumulatePayPointSpend(Long accountId, int spent) {

        if (spent <= 0) {
            return;
        }

        missionUseCase.increaseMissionProgressUseCase(IncreaseMissionProgressCommand.builder()
                .accountId(accountId)
                .actionCode(MissionActionCode.PAY_POINT_SPEND)
                .amount(spent)
                .build());
    }
}
