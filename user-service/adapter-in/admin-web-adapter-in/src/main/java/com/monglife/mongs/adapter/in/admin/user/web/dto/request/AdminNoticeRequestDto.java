package com.monglife.mongs.adapter.in.admin.user.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminNoticeRequestDto {

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String content;
}
