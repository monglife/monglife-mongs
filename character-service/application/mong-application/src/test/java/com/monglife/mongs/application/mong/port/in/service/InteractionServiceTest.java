package com.monglife.mongs.application.mong.port.in.service;

import com.monglife.core.vo.page.PageResult;
import com.monglife.mongs.application.mong.port.exception.InvalidUseInventoryItemException;
import com.monglife.mongs.application.mong.port.exception.NotExistsMongException;
import com.monglife.mongs.application.mong.port.in.InteractionUseCase;
import com.monglife.mongs.application.mong.port.in.MissionUseCase;
import com.monglife.mongs.application.mong.port.in.command.*;
import com.monglife.mongs.application.mong.port.in.utils.MongTestUtil;
import com.monglife.mongs.application.mong.port.out.MongEventPort;
import com.monglife.mongs.application.mong.port.out.MongPersistencePort;
import com.monglife.mongs.application.mong.port.out.MongReadPort;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import com.monglife.mongs.domain.mong.exception.InvalidMongStateException;
import com.monglife.mongs.domain.mong.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InteractionServiceTest {

    private final MongPersistencePort mongPersistencePort = Mockito.mock(MongPersistencePort.class);
    private final MongEventPort mongEventPort = Mockito.mock(MongEventPort.class);
    private final MongReadPort mongReadPort = Mockito.mock(MongReadPort.class);
    private final MissionUseCase missionUseCase = Mockito.mock(MissionUseCase.class);
    private final InteractionUseCase interactionUseCase = new InteractionService(mongPersistencePort, mongReadPort, mongEventPort, missionUseCase);

    @Nested
    @DisplayName("음식 목록 조회 단위 테스트")
    class GetFoodsUseCase {

        @Test
        @DisplayName("섭취 가능 여부를 확인할 수 있는 음식 목록을 조회 한다.")
        void getFoods() {
            // arrange
            final int price = 100;
            final boolean isCanBuy = true;
            final double status = 100;
            final List<Food> foods = List.of(MongTestUtil.getFood(price, isCanBuy, status));
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;

            Mockito.when(mongReadPort.getFoodsPort(mongId)).thenReturn(foods);
            Mockito.when(mongReadPort.getMongPort(mongId)).thenReturn(Optional.of(MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus)));

            // act
            GetFoodsCommand command = GetFoodsCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            var expected = interactionUseCase.getFoodsUseCase(command);

            // assert
            assertIterableEquals(expected, foods);
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 에외가 발생 한다.")
        void getFoodsWhenNotExistsMong() {
            // arrange
            final int price = 100;
            final boolean isCanBuy = true;
            final double status = 100;
            final List<Food> foods = List.of(MongTestUtil.getFood(price, isCanBuy, status));
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongReadPort.getFoodsPort(mongId)).thenReturn(foods);
            Mockito.when(mongReadPort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            GetFoodsCommand command = GetFoodsCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> interactionUseCase.getFoodsUseCase(command));
        }
    }

    @Nested
    @DisplayName("간식 목록 조회 단위 테스트")
    class GetSnacksUseCase {

        @Test
        @DisplayName("섭취 가능 여부를 확인할 수 있는 간식 목록을 조회 한다.")
        void getSnacks() {
            // arrange
            final int price = 100;
            final boolean isCanBuy = true;
            final double status = 100;
            final List<Snack> snacks = List.of(MongTestUtil.getSnack(price, isCanBuy, status));
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;

            Mockito.when(mongReadPort.getSnacksPort(mongId)).thenReturn(snacks);
            Mockito.when(mongReadPort.getMongPort(mongId)).thenReturn(Optional.of(MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus)));

            // act
            GetSnacksCommand command = GetSnacksCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            var expected = interactionUseCase.getSnacksUseCase(command);

            // assert
            assertIterableEquals(expected, snacks);
        }

        @Test
        @DisplayName("몽이 존재하지 않는 경우 에외가 발생 한다.")
        void getFoodsWhenNotExistsMong() {
            // arrange
            final int price = 100;
            final boolean isCanBuy = true;
            final double status = 100;
            final List<Snack> snacks = List.of(MongTestUtil.getSnack(price, isCanBuy, status));
            final long mongId = 1L;
            final long accountId = 1L;

            Mockito.when(mongReadPort.getSnacksPort(mongId)).thenReturn(snacks);
            Mockito.when(mongReadPort.getMongPort(mongId)).thenReturn(Optional.empty());

            // act & assert
            GetSnacksCommand command = GetSnacksCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(NotExistsMongException.class, () -> interactionUseCase.getSnacksUseCase(command));
        }
    }

    @Nested
    @DisplayName("음식 섭취 단위 테스트")
    class FeedFoodUseCase {

        @Test
        @DisplayName("몽이 음식을 섭취 한다.")
        void feedFood() {
            // arrange
            final int price = 50;
            final boolean isCanBuy = true;
            final Food food = MongTestUtil.getFood(price, isCanBuy, 10D);
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final double status = 50;
            final int payPoint = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, status, maxStatus, payPoint);

            Mockito.when(mongReadPort.getFoodPort(food.getFoodCode(), mongId)).thenReturn(Optional.of(food));
            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.createMongFeedFoodHistoryPort(mongId, food.getFoodCode())).thenReturn(Optional.of(food.getFoodCode()));

            // act
            FeedFoodCommand command = FeedFoodCommand.builder()
                    .foodCode(food.getFoodCode())
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            var expected = interactionUseCase.feedFoodUseCase(command);

            // assert
            assertEquals(payPoint - price, expected.getPayPoint());
            assertEquals(status + food.getStrength(), expected.getStrength());
            assertEquals(status + food.getSatiety(), expected.getSatiety());
            assertEquals(status + food.getHealthy(), expected.getHealthy());
            assertEquals(status + food.getFatigue(), expected.getFatigue());
            assertEquals(status + food.getWeight(), expected.getWeight());
        }

        @Test
        @DisplayName("몽 레벨이 0인 경우 (알 상태인 경우) 예외가 발생 한다.")
        void feedFoodWhenEggMong() {
            // arrange
            final int price = 50;
            final boolean isCanBuy = true;
            final Food food = MongTestUtil.getFood(price, isCanBuy, 10D);
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            Mockito.when(mongReadPort.getFoodPort(food.getFoodCode(), mongId)).thenReturn(Optional.of(food));
            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.createMongFeedFoodHistoryPort(mongId, food.getFoodCode())).thenReturn(Optional.of(food.getFoodCode()));

            // act & assert
            FeedFoodCommand command = FeedFoodCommand.builder()
                    .foodCode(food.getFoodCode())
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> interactionUseCase.feedFoodUseCase(command));
        }

        @Test
        @DisplayName("몽 자는 상태인 경우 예외가 발생 한다.")
        void feedFoodWhenMongIsSleeping() {
            // arrange
            final int price = 50;
            final boolean isCanBuy = true;
            final Food food = MongTestUtil.getFood(price, isCanBuy, 10D);
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            // 자는 상태로 변경
            mong.sleep();

            Mockito.when(mongReadPort.getFoodPort(food.getFoodCode(), mongId)).thenReturn(Optional.of(food));
            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.createMongFeedFoodHistoryPort(mongId, food.getFoodCode())).thenReturn(Optional.of(food.getFoodCode()));

            // act & assert
            FeedFoodCommand command = FeedFoodCommand.builder()
                    .foodCode(food.getFoodCode())
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> interactionUseCase.feedFoodUseCase(command));
        }

        @Test
        @DisplayName("몽 사망 상태인 경우 예외가 발생 한다.")
        void feedFoodWhenMongIsDead() {
            // arrange
            final int price = 50;
            final boolean isCanBuy = true;
            final Food food = MongTestUtil.getFood(price, isCanBuy, 10D);
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            // 사망 상태로 변경
            mong.dead();

            Mockito.when(mongReadPort.getFoodPort(food.getFoodCode(), mongId)).thenReturn(Optional.of(food));
            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.createMongFeedFoodHistoryPort(mongId, food.getFoodCode())).thenReturn(Optional.of(food.getFoodCode()));

            // act & assert
            FeedFoodCommand command = FeedFoodCommand.builder()
                    .foodCode(food.getFoodCode())
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> interactionUseCase.feedFoodUseCase(command));
        }
    }

    @Nested
    @DisplayName("간식 섭취 단위 테스트")
    class FeedSnackUseCase {

        @Test
        @DisplayName("몽이 간식을 섭취 한다.")
        void feedSnack() {
            // arrange
            final int price = 50;
            final boolean isCanBuy = true;
            final Snack snack = MongTestUtil.getSnack(price, isCanBuy, 10D);
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final double status = 50;
            final int payPoint = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, status, maxStatus, payPoint);

            Mockito.when(mongReadPort.getSnackPort(snack.getSnackCode(), mongId)).thenReturn(Optional.of(snack));
            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.createMongFeedSnackHistoryPort(mongId, snack.getSnackCode())).thenReturn(Optional.of(snack.getSnackCode()));

            // act
            FeedSnackCommand command = FeedSnackCommand.builder()
                    .snackCode(snack.getSnackCode())
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            var expected = interactionUseCase.feedSnackUseCase(command);

            // assert
            assertEquals(payPoint - price, expected.getPayPoint());
            assertEquals(status + snack.getStrength(), expected.getStrength());
            assertEquals(status + snack.getSatiety(), expected.getSatiety());
            assertEquals(status + snack.getHealthy(), expected.getHealthy());
            assertEquals(status + snack.getFatigue(), expected.getFatigue());
            assertEquals(status + snack.getWeight(), expected.getWeight());
        }

        @Test
        @DisplayName("몽 레벨이 0인 경우 (알 상태인 경우) 예외가 발생 한다.")
        void feedFoodWhenEggMong() {
            // arrange
            final int price = 50;
            final boolean isCanBuy = true;
            final Snack snack = MongTestUtil.getSnack(price, isCanBuy, 10D);
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final Mong mong = MongTestUtil.getEggMong(mongId, accountId, maxStatus);

            Mockito.when(mongReadPort.getSnackPort(snack.getSnackCode(), mongId)).thenReturn(Optional.of(snack));
            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when((mongPersistencePort.saveMongPort(mong))).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.createMongFeedSnackHistoryPort(mongId, snack.getSnackCode())).thenReturn(Optional.of(snack.getSnackCode()));

            // act & assert
            FeedSnackCommand command = FeedSnackCommand.builder()
                    .snackCode(snack.getSnackCode())
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> interactionUseCase.feedSnackUseCase(command));
        }

        @Test
        @DisplayName("몽 자는 상태인 경우 예외가 발생 한다.")
        void feedFoodWhenMongIsSleeping() {
            // arrange
            final int price = 50;
            final boolean isCanBuy = true;
            final Snack snack = MongTestUtil.getSnack(price, isCanBuy, 10D);
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            // 수면 상태로 변경
            mong.sleep();

            Mockito.when(mongReadPort.getSnackPort(snack.getSnackCode(), mongId)).thenReturn(Optional.of(snack));
            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when((mongPersistencePort.saveMongPort(mong))).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.createMongFeedSnackHistoryPort(mongId, snack.getSnackCode())).thenReturn(Optional.of(snack.getSnackCode()));

            // act & assert
            FeedSnackCommand command = FeedSnackCommand.builder()
                    .snackCode(snack.getSnackCode())
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> interactionUseCase.feedSnackUseCase(command));
        }

        @Test
        @DisplayName("몽 사망 상태인 경우 예외가 발생 한다.")
        void feedFoodWhenMongIsDead() {
            // arrange
            final int price = 50;
            final boolean isCanBuy = true;
            final Snack snack = MongTestUtil.getSnack(price, isCanBuy, 10D);
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);

            // 사망 상태로 변경
            mong.dead();

            Mockito.when(mongReadPort.getSnackPort(snack.getSnackCode(), mongId)).thenReturn(Optional.of(snack));
            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when((mongPersistencePort.saveMongPort(mong))).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.createMongFeedSnackHistoryPort(mongId, snack.getSnackCode())).thenReturn(Optional.of(snack.getSnackCode()));

            // act & assert
            FeedSnackCommand command = FeedSnackCommand.builder()
                    .snackCode(snack.getSnackCode())
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(InvalidMongStateException.class, () -> interactionUseCase.feedSnackUseCase(command));
        }
    }

    @Nested
    @DisplayName("인벤 아이템 목록 조회 단위 테스트")
    class GetInventoriesUseCase {

        @Test
        @DisplayName("인벤토리 아이템 목록을 조회 한다.")
        void getInventories() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, maxStatus);
            final long inventoryItemId = 1L;
            final String typeCode = "TEST-INVENTORY-ITEM-TYPE-CODE";
            final String typeName = "TEST-INVENTORY-ITEM-TYPE-NAME";
            final PageResult<Inventory> inventories = PageResult.<Inventory>builder()
                    .page(1)
                    .size(1)
                    .isLastPage(true)
                    .totalPage(1)
                    .result(List.of(
                            Inventory.builder()
                                    .inventoryId(inventoryItemId)
                                    .mongId(mongId)
                                    .inventoryTypeCode(InventoryTypeCode.FOOD)
                                    .inventoryCode(typeCode)
                                    .inventoryName(typeName)
                                    .build()))
                    .build();

            Mockito.when(mongReadPort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getInventoriesPort(mongId, inventories.getPage(), inventories.getSize())).thenReturn(inventories);

            // act
            GetInventoriesCommand command = GetInventoriesCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .page(inventories.getPage())
                    .size(inventories.getSize())
                    .build();

            var expected = interactionUseCase.getInventoriesUseCase(command);

            // assert
            assertIterableEquals(expected.getResult(), inventories.getResult());
        }
    }

    @Nested
    @DisplayName("인벤 소비성 아이템 사용 단위 테스트")
    class UseInventoryUseCase {

        @Test
        @DisplayName("소비성 인벤토리 아이템을 사용하여 음식을 섭취 한다.")
        void useInventoryWhenFeedFood() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final double status = 50;
            final int payPoint = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, status, maxStatus, payPoint);
            final int price = 50;
            final boolean isCanBuy = true;
            final Food food = MongTestUtil.getFood(price, isCanBuy, 10D);
            final long inventoryId = 1L;
            final Inventory inventory = Inventory.builder()
                    .inventoryId(inventoryId)
                    .mongId(mongId)
                    .inventoryTypeCode(InventoryTypeCode.FOOD)
                    .inventoryCode(food.getFoodCode())
                    .inventoryName(food.getFoodName())
                    .build();

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.getInventoryPort(mongId)).thenReturn(Optional.of(inventory));
            Mockito.when(mongReadPort.getFoodPort(food.getFoodCode(), mongId)).thenReturn(Optional.of(food));
            Mockito.when((mongPersistencePort.saveMongPort(mong))).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.deleteInventoryPort(inventoryId)).thenReturn(Optional.of(inventory));

            // act
            UseInventoryCommand command = UseInventoryCommand.builder()
                    .inventoryId(inventoryId)
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            var expected = interactionUseCase.useInventoryUseCase(command);

            // assert
            assertEquals(payPoint, mong.getPayPoint());
            assertEquals(status + food.getStrength(), expected.getStrength());
            assertEquals(status + food.getSatiety(), expected.getSatiety());
            assertEquals(status + food.getHealthy(), expected.getHealthy());
            assertEquals(status + food.getFatigue(), expected.getFatigue());
            assertEquals(status + food.getWeight(), expected.getWeight());
        }

        @Test
        @DisplayName("소비성 인벤토리 아이템을 사용하여 간식을 섭취 한다.")
        void useInventoryWhenFeedSnack() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final double status = 50;
            final int payPoint = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, status, maxStatus, payPoint);
            final int price = 50;
            final boolean isCanBuy = true;
            final Snack snack = MongTestUtil.getSnack(price, isCanBuy, 10D);
            final long inventoryId = 1L;
            final Inventory inventory = Inventory.builder()
                    .inventoryId(inventoryId)
                    .mongId(mongId)
                    .inventoryTypeCode(InventoryTypeCode.SNACK)
                    .inventoryCode(snack.getSnackCode())
                    .inventoryName(snack.getSnackName())
                    .build();

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.getInventoryPort(mongId)).thenReturn(Optional.of(inventory));
            Mockito.when(mongReadPort.getSnackPort(snack.getSnackCode(), mongId)).thenReturn(Optional.of(snack));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.deleteInventoryPort(inventoryId)).thenReturn(Optional.of(inventory));

            // act
            UseInventoryCommand command = UseInventoryCommand.builder()
                    .inventoryId(inventoryId)
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            var expected = interactionUseCase.useInventoryUseCase(command);

            // assert
            assertEquals(payPoint, mong.getPayPoint());
            assertEquals(status + snack.getStrength(), expected.getStrength());
            assertEquals(status + snack.getSatiety(), expected.getSatiety());
            assertEquals(status + snack.getHealthy(), expected.getHealthy());
            assertEquals(status + snack.getFatigue(), expected.getFatigue());
            assertEquals(status + snack.getWeight(), expected.getWeight());
        }

        @Test
        @DisplayName("소비성 아이템이 아닌 경우 예외가 발생 한다.")
        void useInventoryWhenNotConsumedInventoryItem() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final double status = 50;
            final int payPoint = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, status, maxStatus, payPoint);
            final long inventoryId = 1L;
            final String inventoryCode = "TEST-TYPE-CODE";
            final String inventoryName = "TEST-TYPE-NAME";
            final Inventory inventory = Inventory.builder()
                    .inventoryId(inventoryId)
                    .mongId(mongId)
                    .inventoryTypeCode(InventoryTypeCode.MAP)
                    .inventoryCode(inventoryCode)
                    .inventoryName(inventoryName)
                    .build();

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.getInventoryPort(mongId)).thenReturn(Optional.of(inventory));

            // act & assert
            UseInventoryCommand command = UseInventoryCommand.builder()
                    .inventoryId(inventoryId)
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            assertThrows(InvalidUseInventoryItemException.class, () -> interactionUseCase.useInventoryUseCase(command));
        }
    }

    @Nested
    @DisplayName("랜덤 뽑기 티켓 구매 단위 테스트")
    class BuyRandomDrawTicketUseCase {

        @Test
        @DisplayName("랜덤 뽑기 티켓을 구매 한다.")
        void buyRandomDrawTicket() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final double status = 50;
            final int payPoint = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, status, maxStatus, payPoint);

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            BuyRandomDrawTicketCommand command = BuyRandomDrawTicketCommand.builder()
                    .accountId(accountId)
                    .mongId(mongId)
                    .build();


            var expected = interactionUseCase.buyRandomDrawTicketUseCase(command);

            // assert
            assertTrue(payPoint > expected.getPayPoint());
            assertTrue(0 < expected.getRandomDrawTicketCount());
        }
    }

    @Nested
    @DisplayName("랜덤 뽑기 단위 테스트")
    class RandomDrawUseCase {

        @Test
        @DisplayName("랜덤 뽑기를 통해 소비성 아이템을 뽑고 인벤토리 아이템으로 등록 한다.")
        void randomDraw() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final double status = 50;
            final int payPoint = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, status, maxStatus, payPoint);
            final long inventoryId = 1L;
            final String inventoryCode = "TEST-TYPE-CODE";
            final String inventoryName = "TEST-TYPE-NAME";
            final Inventory inventory = Inventory.builder()
                    .inventoryId(inventoryId)
                    .mongId(mongId)
                    .inventoryTypeCode(InventoryTypeCode.FOOD)
                    .inventoryCode(inventoryCode)
                    .inventoryName(inventoryName)
                    .build();
            final List<RandomDraw> randomDraws = List.of(
                    RandomDraw.builder()
                            .randomDrawId(inventoryId)
                            .inventoryTypeCode(inventory.getInventoryTypeCode())
                            .randomDrawCode(inventory.getInventoryCode())
                            .randomDrawName(inventory.getInventoryName())
                            .build());

            mong.buyRandomDrawTicket();

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getRandomDrawsPort(accountId)).thenReturn(randomDraws);
            Mockito.when(mongPersistencePort.createInventoryPort(Mockito.any())).thenReturn(Optional.of(inventory));
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            RandomDrawCommand command = RandomDrawCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            var expected = interactionUseCase.randomDrawUseCase(command);

            // assert
            assertEquals(inventoryId, expected.getRandomDrawId());
            assertEquals(inventoryCode, expected.getRandomDrawCode());
            assertEquals(inventoryName, expected.getRandomDrawName());
        }

        @Test
        @DisplayName("랜덤 뽑기를 통해 맵 아이템을 뽑고 컬렉션 맵 등록 이벤트를 발생 시킨다.")
        void randomDrawWhenDrawMap() {
            // arrange
            final long mongId = 1L;
            final long accountId = 1L;
            final double maxStatus = 100;
            final double status = 50;
            final int payPoint = 100;
            final Mong mong = MongTestUtil.getFirstLevelMong(mongId, accountId, status, maxStatus, payPoint);
            final long inventoryId = 1L;
            final String inventoryCode = "TEST-TYPE-CODE";
            final String inventoryName = "TEST-TYPE-NAME";
            final Inventory inventory = Inventory.builder()
                    .inventoryId(inventoryId)
                    .mongId(mongId)
                    .inventoryTypeCode(InventoryTypeCode.MAP)
                    .inventoryCode(inventoryCode)
                    .inventoryName(inventoryName)
                    .build();
            final List<RandomDraw> randomDraws = List.of(
                    RandomDraw.builder()
                            .randomDrawId(inventoryId)
                            .inventoryTypeCode(inventory.getInventoryTypeCode())
                            .randomDrawCode(inventory.getInventoryCode())
                            .randomDrawName(inventory.getInventoryName())
                            .build());

            mong.buyRandomDrawTicket();

            Mockito.when(mongPersistencePort.getMongPort(mongId)).thenReturn(Optional.of(mong));
            Mockito.when(mongReadPort.getRandomDrawsPort(accountId)).thenReturn(randomDraws);
            Mockito.when(mongPersistencePort.saveMongPort(mong)).thenReturn(Optional.of(mong));

            // act
            RandomDrawCommand command = RandomDrawCommand.builder()
                    .mongId(mongId)
                    .accountId(accountId)
                    .build();

            var expected = interactionUseCase.randomDrawUseCase(command);

            // assert
            Mockito.verify(mongEventPort).randomDrawMapEventPort(accountId, inventoryCode);
            assertEquals(inventoryId, expected.getRandomDrawId());
            assertEquals(inventoryCode, expected.getRandomDrawCode());
            assertEquals(inventoryName, expected.getRandomDrawName());
        }
    }
}