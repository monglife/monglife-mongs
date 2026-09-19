package com.monglife.mongs.domain.mission.model;

import com.monglife.mongs.domain.mission.enums.MissionRewardTypeCode;
import com.monglife.mongs.domain.mission.exception.InvalidMissionRewardException;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MissionRewardTest {

    @Nested
    @DisplayName("미션 리워드 검증 단위 테스트")
    class Verify {

        @Test
        @DisplayName("수량이 0 이하면 예외가 발생 한다.")
        void verifyWhenAmountIsNotPositive() {
            // arrange
            final MissionReward reward = MissionReward.builder()
                    .rewardTypeCode(MissionRewardTypeCode.PAY_POINT)
                    .amount(0)
                    .build();

            // act & assert
            assertThrows(InvalidMissionRewardException.class, reward::verify);
        }

        @Test
        @DisplayName("인벤토리 리워드에 아이템 코드가 없으면 예외가 발생 한다.")
        void verifyWhenInventoryWithoutCode() {
            // arrange
            final MissionReward reward = MissionReward.builder()
                    .rewardTypeCode(MissionRewardTypeCode.INVENTORY)
                    .inventoryTypeCode(InventoryTypeCode.FOOD)
                    .amount(1)
                    .build();

            // act & assert
            assertThrows(InvalidMissionRewardException.class, reward::verify);
        }

        @Test
        @DisplayName("맵은 몽 인벤토리가 아니라 리워드로 쓸 수 없다.")
        void verifyWhenInventoryTypeIsMap() {
            // arrange
            final MissionReward reward = MissionReward.builder()
                    .rewardTypeCode(MissionRewardTypeCode.INVENTORY)
                    .rewardCode("MP000")
                    .inventoryTypeCode(InventoryTypeCode.MAP)
                    .amount(1)
                    .build();

            // act & assert
            assertThrows(InvalidMissionRewardException.class, reward::verify);
        }

        @Test
        @DisplayName("음식 인벤토리 리워드는 통과 한다.")
        void verifyFoodInventory() {
            // arrange
            final MissionReward reward = MissionReward.builder()
                    .rewardTypeCode(MissionRewardTypeCode.INVENTORY)
                    .rewardCode("FD000")
                    .inventoryTypeCode(InventoryTypeCode.FOOD)
                    .amount(2)
                    .build();

            // act & assert
            assertDoesNotThrow(reward::verify);
        }

        @Test
        @DisplayName("스타 포인트 리워드는 아이템 정보 없이 통과 한다.")
        void verifyStarPoint() {
            // arrange
            final MissionReward reward = MissionReward.builder()
                    .rewardTypeCode(MissionRewardTypeCode.STAR_POINT)
                    .amount(3)
                    .build();

            // act & assert
            assertDoesNotThrow(reward::verify);
        }
    }
}
