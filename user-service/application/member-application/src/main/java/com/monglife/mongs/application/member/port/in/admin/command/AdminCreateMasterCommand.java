package com.monglife.mongs.application.member.port.in.admin.command;

import lombok.Builder;
import lombok.Getter;

/**
 * 마스터 데이터 등록. 종류마다 쓰는 필드가 달라 한 커맨드에 모았다.
 * {@code code}/{@code name} 은 공통 코드(monglife_comn_code) 가 된다.
 */
@Getter
public class AdminCreateMasterCommand {

    private final String code;

    private final String name;

    /* 맵 */
    private final String words;

    /* 스타 포인트 환전 상품 */
    private final Integer starPoint;

    @Builder
    public AdminCreateMasterCommand(String code, String name, String words, Integer starPoint) {
        this.code = code;
        this.name = name;
        this.words = words;
        this.starPoint = starPoint;
    }
}
