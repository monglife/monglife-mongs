package com.monglife.mongs.domain.member.model;

import com.monglife.mongs.domain.member.exception.AlreadyMaxSlotCountException;
import com.monglife.mongs.domain.member.exception.NotEnoughStarPointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerAdminTest {

    private Player player(int slotCount, int starPoint) {
        return Player.builder()
                .accountId(1L)
                .slotCount(slotCount)
                .starPoint(starPoint)
                .build();
    }

    @Nested
    @DisplayName("관리자 스타 포인트 가감 단위 테스트")
    class AdjustStarPoint {

        @Test
        @DisplayName("스타 포인트를 증가 시킨다.")
        void increase() {
            // arrange
            final Player player = player(1, 10);

            // act
            player.adjustStarPoint(5);

            // assert
            assertEquals(15, player.getStarPoint());
        }

        @Test
        @DisplayName("스타 포인트를 차감 한다.")
        void decrease() {
            // arrange
            final Player player = player(1, 10);

            // act
            player.adjustStarPoint(-4);

            // assert
            assertEquals(6, player.getStarPoint());
        }

        @Test
        @DisplayName("0 아래로 내려가는 차감은 예외가 발생 한다.")
        void belowZero() {
            // arrange
            final Player player = player(1, 3);

            // act & assert
            assertThrows(NotEnoughStarPointException.class, () -> player.adjustStarPoint(-4));
            assertEquals(3, player.getStarPoint());
        }
    }

    @Nested
    @DisplayName("관리자 슬롯 수 수정 단위 테스트")
    class UpdateSlotCount {

        @Test
        @DisplayName("스타 포인트 차감 없이 슬롯 수를 지정 한다.")
        void updateSlotCount() {
            // arrange
            final Player player = player(1, 0);

            // act
            player.updateSlotCount(Player.getMaxSlotCount());

            // assert
            assertEquals(Player.getMaxSlotCount(), player.getSlotCount());
            assertEquals(0, player.getStarPoint());
        }

        @Test
        @DisplayName("최대 슬롯 수를 넘으면 예외가 발생 한다.")
        void exceedMax() {
            // arrange
            final Player player = player(1, 0);

            // act & assert
            assertThrows(AlreadyMaxSlotCountException.class, () -> player.updateSlotCount(Player.getMaxSlotCount() + 1));
        }
    }
}
