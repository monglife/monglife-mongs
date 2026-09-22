package com.monglife.mongs.adapter.transaction;

import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class MissionRewardStarPointEventDto {

    private Long accountId;

    private Integer starPoint;

    /** 소비 측 로그에서 어느 미션의 지급인지 찾기 위한 값 */
    private String missionCode;

    @Builder
    public MissionRewardStarPointEventDto(Long accountId, Integer starPoint, String missionCode) {
        this.accountId = accountId;
        this.starPoint = starPoint;
        this.missionCode = missionCode;
    }
}
