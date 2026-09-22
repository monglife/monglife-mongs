package com.monglife.mongs.adapter.in.admin.character.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminInventoryGrantRequestDto {

    /** 공통 코드(음식: FDxxx, 간식: SNxxx) */
    @NotBlank
    @Size(max = 32)
    private String inventoryCode;

    @NotNull
    private InventoryTypeCode inventoryTypeCode;
}
