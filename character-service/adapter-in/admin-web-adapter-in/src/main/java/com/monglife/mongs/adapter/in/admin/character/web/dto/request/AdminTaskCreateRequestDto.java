package com.monglife.mongs.adapter.in.admin.character.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 스케줄 등록.
 *
 * <p>고정 시각(수면·기상)은 받지 않는다. 몽의 sleepAt/wakeupAt 을 쓴다 - 여기서 따로 받으면
 * 몽 설정과 어긋난 시각에 도는 스케줄이 생긴다.
 *
 * <p>enum 이 아니라 {@code String} 으로 받는다. enum 으로 두면 Jackson 이 <b>이름</b>으로
 * 바인딩하는데, 목록 API 는 <b>코드</b>(DECREASE-STATUS)를 돌려준다. 읽은 값을 그대로 쓰지
 * 못하는 API 가 되고, 파싱 실패도 응답 코드 없는 Spring 기본 봉투로 나간다.
 * {@code MongSchedulerType.fromCode} 가 둘 다 받아 준다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminTaskCreateRequestDto {

    @NotBlank
    private String schedulerTypeCode;
}
