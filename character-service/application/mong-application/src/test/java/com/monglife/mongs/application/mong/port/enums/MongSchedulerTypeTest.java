package com.monglife.mongs.application.mong.port.enums;

import com.monglife.mongs.application.mong.port.exception.InvalidSchedulerTypeCodeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 스케줄 타입 코드 파싱 단위 테스트.
 *
 * <p>목록 응답은 코드(DECREASE-STATUS)를 주는데 등록 요청은 enum 이름(DECREASE_STATUS)으로
 * 바인딩하던 문제를 막는다. 7종 중 4종은 둘이 다르고, 같은 3종(SLEEP/WAKEUP/DEAD) 때문에
 * 한동안 드러나지 않았다.
 */
class MongSchedulerTypeTest {

    @Test
    @DisplayName("목록 응답이 주는 코드로 파싱된다. 읽은 값을 그대로 등록에 쓸 수 있어야 한다.")
    void fromCodeAcceptsCode() {
        for (MongSchedulerType type : MongSchedulerType.values()) {
            assertEquals(type, MongSchedulerType.fromCode(type.getCode()), type.getCode());
        }
    }

    @Test
    @DisplayName("enum 이름으로도 파싱된다. 관리자 웹이 아직 이름을 보내도 깨지지 않아야 한다.")
    void fromCodeAcceptsEnumName() {
        for (MongSchedulerType type : MongSchedulerType.values()) {
            assertEquals(type, MongSchedulerType.fromCode(type.name()), type.name());
        }
    }

    @Test
    @DisplayName("코드와 이름이 다른 4종이 각각 제 타입으로 간다.")
    void fromCodeResolvesDivergedTypes() {
        assertEquals(MongSchedulerType.EGG_EVOLUTION, MongSchedulerType.fromCode("EGG-EVOLUTION"));
        assertEquals(MongSchedulerType.INCREASE_STATUS, MongSchedulerType.fromCode("INCREASE-STATUS"));
        assertEquals(MongSchedulerType.DECREASE_STATUS, MongSchedulerType.fromCode("DECREASE-STATUS"));
        assertEquals(MongSchedulerType.INCREASE_POOP, MongSchedulerType.fromCode("INCREASE-POOP"));
    }

    @Test
    @DisplayName("코드와 이름을 통틀어 겹치는 값이 없다. 둘 다 받아도 모호하지 않다.")
    void codesAndNamesDoNotCollide() {
        Set<String> seen = new HashSet<>();

        Arrays.stream(MongSchedulerType.values()).forEach(type -> {
            seen.add(type.getCode());
            seen.add(type.name());
        });

        // 7종 × 2개 - 이름과 코드가 같은 3종(SLEEP/WAKEUP/DEAD) 중복 3개 = 11
        assertEquals(11, seen.size());
    }

    @Test
    @DisplayName("알 수 없는 값은 타입 있는 예외로 거절한다. Spring 기본 400 봉투로 새면 안 된다.")
    void fromCodeRejectsUnknown() {
        List<String> invalid = Arrays.asList("NOPE", "decrease-status", "DECREASE STATUS", "", null);

        for (String value : invalid) {
            assertThrows(InvalidSchedulerTypeCodeException.class,
                    () -> MongSchedulerType.fromCode(value), String.valueOf(value));
        }
    }
}
