package com.monglife.mongs.adapter.in.mong.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClaimMissionRewardRequestDto {

    /** 리워드를 받을 몽. 경험치·페이 포인트·인벤토리가 몽 소유라 대상이 필요하다 */
    @Min(1)
    @NotNull
    private Long mongId;

    @Builder
    public ClaimMissionRewardRequestDto(Long mongId) {
        this.mongId = mongId;
    }
}
