package com.monglife.mongs.adapter.in.admin.user.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monglife.mongs.application.member.port.in.admin.AdminMemberMasterUseCase;
import jakarta.validation.constraints.Min;
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
public class AdminMasterCreateRequestDto {

    @NotNull
    private AdminMemberMasterUseCase.Kind kind;

    /** 공통 코드. 환전 상품은 이 값이 상품 ID 가 된다 */
    @NotBlank
    @Size(max = 32)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    /** 맵 탐색 키워드 */
    @Size(max = 500)
    private String words;

    @Min(1)
    private Integer starPoint;
}
