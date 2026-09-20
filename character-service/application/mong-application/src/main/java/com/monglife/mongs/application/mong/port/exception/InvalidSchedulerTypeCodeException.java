package com.monglife.mongs.application.mong.port.exception;

import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.application.mong.port.errorCode.ApplicationMongAdminErrorCode;
import lombok.Getter;

import java.util.Collections;

/**
 * 알아볼 수 없는 스케줄 타입 코드.
 *
 * <p>예전에는 요청 DTO 가 enum 을 직접 받아서 Jackson 이 이름으로 바인딩했다. 그래서 목록 API 가
 * 돌려준 코드(DECREASE-STATUS)를 그대로 등록에 쓰면 존재하지 않는 코드와 똑같이 거부됐고,
 * 응답도 Spring 기본 봉투라 원인을 알 수 없었다. 이제 어댑터가 파싱하고 이 예외로 답한다.
 */
@Getter
public class InvalidSchedulerTypeCodeException extends ErrorException {

    public InvalidSchedulerTypeCodeException() {
        this.errorCode = ApplicationMongAdminErrorCode.INVALID_SCHEDULER_TYPE_CODE;
        this.result = Collections.emptyMap();
    }
}
