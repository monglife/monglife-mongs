package com.monglife.mongs.adapter.in.admin.user.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminStarPointRequestDto {

    /** 가감할 스타 포인트. 음수면 차감 */
    @NotNull
    private Integer delta;

    @Size(max = 500)
    private String reason;
}
