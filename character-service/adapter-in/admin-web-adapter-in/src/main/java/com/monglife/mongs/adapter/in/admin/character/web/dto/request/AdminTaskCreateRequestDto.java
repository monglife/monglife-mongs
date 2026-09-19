package com.monglife.mongs.adapter.in.admin.character.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.monglife.mongs.application.mong.port.enums.MongSchedulerType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 스케줄 등록.
 *
 * <p>고정 시각(수면·기상)은 받지 않는다. 몽의 sleepAt/wakeupAt 을 쓴다 - 여기서 따로 받으면
 * 몽 설정과 어긋난 시각에 도는 스케줄이 생긴다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminTaskCreateRequestDto {

    @NotNull
    private MongSchedulerType schedulerTypeCode;
}
