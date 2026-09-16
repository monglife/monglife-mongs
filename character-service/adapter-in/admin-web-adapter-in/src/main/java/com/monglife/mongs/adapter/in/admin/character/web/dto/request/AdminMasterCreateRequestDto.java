package com.monglife.mongs.adapter.in.admin.character.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monglife.mongs.application.mong.port.in.admin.AdminMongMasterUseCase;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 마스터 데이터 등록. 종류마다 채우는 필드가 다르다 — 종류별 필수 항목은 서비스가 검증한다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminMasterCreateRequestDto {

    @NotNull
    private AdminMongMasterUseCase.Kind kind;

    /** 공통 코드. 랜덤 뽑기는 이미 있는 코드를 고른다 */
    @NotBlank
    @Size(max = 32)
    private String code;

    @Size(max = 100)
    private String name;

    /* 몽 타입 */
    @Min(0) private Integer level;
    @Min(0) private Double evolutionScore;
    @Min(0) private Double maxStatus;
    @Size(max = 32) private String groupType;

    /* 음식·간식 */
    @Min(0) private Integer price;
    private Double weight;
    private Double strength;
    private Double satiety;
    private Double healthy;
    private Double fatigue;
    @Min(0) private Integer delaySeconds;

    /* 훈련 */
    @Min(0) private Integer payPoint;
    @Min(0) private Integer score;
    @Min(0) private Integer timeout;
    private Double exp;

    /* 랜덤 뽑기 */
    private InventoryTypeCode inventoryTypeCode;
}
