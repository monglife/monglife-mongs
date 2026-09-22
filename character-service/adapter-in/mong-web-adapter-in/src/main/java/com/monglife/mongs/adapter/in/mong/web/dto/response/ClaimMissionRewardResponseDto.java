package com.monglife.mongs.adapter.in.mong.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ClaimMissionRewardResponseDto {

    private Long accountMissionId;

    private Long mongId;

    private Double expRatio;

    private Integer payPoint;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    @Builder
    public ClaimMissionRewardResponseDto(Long accountMissionId, Long mongId, Double expRatio, Integer payPoint, LocalDateTime updatedAt) {
        this.accountMissionId = accountMissionId;
        this.mongId = mongId;
        this.expRatio = expRatio;
        this.payPoint = payPoint;
        this.updatedAt = updatedAt;
    }
}
