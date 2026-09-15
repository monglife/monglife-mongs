package com.monglife.mongs.adapter.in.admin.character.web.exception;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.core.enums.response.GlobalResponse;
import com.monglife.core.exception.ErrorException;
import com.monglife.mongs.adapter.in.admin.character.web.controller.AdminHealthController;
import com.monglife.mongs.application.mong.port.exception.NotExistsMongException;
import com.monglife.mongs.application.mong.port.exception.NotExistsTaskException;
import com.monglife.mongs.application.battle.port.exception.NotExistsMatchException;
import com.monglife.mongs.application.battle.port.exception.NotExistsQueuePlayerException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 관리자 컨트롤러 패키지에만 적용된다. 없는 자원은 404, 나머지 도메인 예외는 400.
 * 401 은 쓰지 않는다 — 관리자 웹이 401 을 토큰 만료로 보고 재발급을 시도한다.
 */
@RestControllerAdvice(basePackageClasses = AdminHealthController.class)
public class AdminCharacterExceptionHandler {

    @ExceptionHandler({ NotExistsMongException.class, NotExistsTaskException.class, NotExistsMatchException.class, NotExistsQueuePlayerException.class })
    public ResponseEntity<ResponseDto<Map<String, Object>>> handleNotFound(ErrorException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND.value())
                .body(e.getErrorCode().toResponseDto(HttpStatus.NOT_FOUND.value(), e.getResult()));
    }

    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<ResponseDto<Map<String, Object>>> handleErrorException(ErrorException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST.value())
                .body(e.getErrorCode().toResponseDto(HttpStatus.BAD_REQUEST.value(), e.getResult()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Map<String, Object>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        BindingResult bindingResult = e.getBindingResult();
        Set<String> errorFields = new LinkedHashSet<>();
        StringBuilder messageBuilder = new StringBuilder();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            messageBuilder.append("'").append(fieldError.getField()).append("'(은)는 ").append(fieldError.getDefaultMessage()).append(". ");
            errorFields.add(fieldError.getField());
        }

        return ResponseEntity
                .status(GlobalResponse.INVALID_PARAMETER.getHttpStatus())
                .body(GlobalResponse.INVALID_PARAMETER.toResponseDto(Map.of("message", messageBuilder.toString(), "errorFields", errorFields)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseDto<Map<String, Object>>> handleConstraintViolationException(ConstraintViolationException e) {

        Set<String> errorFields = new LinkedHashSet<>();
        StringBuilder messageBuilder = new StringBuilder();

        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String[] pathParts = violation.getPropertyPath().toString().split("\\.");
            String fieldName = pathParts[pathParts.length - 1];
            messageBuilder.append("'").append(fieldName).append("'(은)는 ").append(violation.getMessage()).append(". ");
            errorFields.add(fieldName);
        }

        return ResponseEntity
                .status(GlobalResponse.INVALID_PARAMETER.getHttpStatus())
                .body(GlobalResponse.INVALID_PARAMETER.toResponseDto(Map.of("message", messageBuilder.toString(), "errorFields", errorFields)));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseDto<Map<String, Object>>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ResponseEntity
                .status(GlobalResponse.INVALID_PARAMETER.getHttpStatus())
                .body(GlobalResponse.INVALID_PARAMETER.toResponseDto(Map.of("message", e.getParameterName() + "(은)는 필수 파라미터 입니다.")));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseDto<Map<String, Object>>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {

        String message = e.getPropertyName() + "의 타입";
        message += e.getRequiredType() != null ? "은 '" + e.getRequiredType().getSimpleName() + "' 이여야 합니다." : "이 적절하지 않습니다.";

        return ResponseEntity
                .status(GlobalResponse.INVALID_PARAMETER.getHttpStatus())
                .body(GlobalResponse.INVALID_PARAMETER.toResponseDto(Map.of("message", message)));
    }
}
