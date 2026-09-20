package com.monglife.mongs.domain.battle.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MatchStateCode {
    
    ENTERING("매치 플레이어 입장중"),
    PROCESS("매치 진행중"),
    END("매치 종료"),
    /**
     * 입장 기한을 넘겨 취소됨. 참가비는 돌려준다.
     *
     * <p>{@code END} 와 나눈 이유는 정산이 달라서다. {@code END} 는 치러진 경기라 승자에게
     * 보상이 가고, 이쪽은 아무도 싸우지 않았으니 배팅만 되돌린다. 상태를 합치면 둘을 구분할
     * 방법이 없다 - {@code round=0} 은 판별자가 못 된다. 입장 직후 퇴장해도 {@code round} 는
     * 0 인 채로 {@code END} 가 되고, 그쪽에는 보상이 지급된다.
     */
    CANCELED("매치 취소 (입장 기한 초과)"),
    ;

    private final String message;
}
