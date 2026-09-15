package com.monglife.mongs.adapter.in.admin.character.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** null 인 항목은 건드리지 않는다. 값은 서버가 0 ~ maxStatus 로 잘라 넣는다 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminMongStatusRequestDto {

    @Min(0) private Double weight;
    @Min(0) private Double strength;
    @Min(0) private Double satiety;
    @Min(0) private Double healthy;
    @Min(0) private Double fatigue;
    @Min(0) private Double exp;
    @Min(0) private Integer payPoint;
    @Min(0) private Integer poopCount;
    @Min(0) private Integer randomDrawTicketCount;

    @Size(max = 500)
    private String reason;
}
