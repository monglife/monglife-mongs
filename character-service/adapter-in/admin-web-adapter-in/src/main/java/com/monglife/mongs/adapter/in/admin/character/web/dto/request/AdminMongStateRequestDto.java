package com.monglife.mongs.adapter.in.admin.character.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monglife.mongs.domain.mong.enums.MongStateCode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminMongStateRequestDto {

    @NotNull
    private MongStateCode stateCode;
}
