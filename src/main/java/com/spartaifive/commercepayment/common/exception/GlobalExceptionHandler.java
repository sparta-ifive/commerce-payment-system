package com.spartaifive.commercepayment.common.exception;

import com.spartaifive.commercepayment.common.response.DataMessageResponse;
import com.spartaifive.commercepayment.common.response.MessageResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 전역 예외 처리기 (commerce-payment-system)
 *
 * - 성공 응답: DataResponse.success("OK"/"CREATED", data) (Controller에서 처리)
 * - 실패 응답: MessageResponse.fail("ERR_...", "Message")로 포맷 통일 (여기서 처리)
 *
 * 처리 범위:
 * - 비즈니스 예외 (ServiceErrorException + ErrorCode)
 * - 외부 연동 예외 (PortOneApiException)
 * - Validation / 바인딩 / 파싱 예외 (400)
 * - DB 무결성 예외 (409)
 * - 기타 서버 오류 (500)
 *
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ===========================
       비즈니스 예외 - Service/Domain
       =========================== */
    @ExceptionHandler(ServiceErrorException.class)
    public ResponseEntity<MessageResponse<?>> handleServiceError(ServiceErrorException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(MessageResponse.fail(code.name(), e.getMessage()));
    }

    /* ===========================
       비즈니스 예외 - Service/Domain
       Data 포함
       =========================== */
    @ExceptionHandler(ServiceDataErrorException.class)
    public ResponseEntity<DataMessageResponse<?>> handleServiceError(ServiceDataErrorException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.httpStatus())
                .body(DataMessageResponse.fail(code.name(), e.getMessage(), e.getData()));
    }

    /* ===========================
       400 Bad Request - Validation (@RequestBody)
       - @Valid @RequestBody DTO 검증 실패 시 발생
       =========================== */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse<?>> handleValidation(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().isEmpty()
        ? ErrorCode.ERR_NOT_VALID_VALUE.message()
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MessageResponse.fail(ErrorCode.ERR_NOT_VALID_VALUE.name(), message));
    }

    /* ===========================
       400 Bad Request - Validation (@RequestParam/@PathVariable)
       - @Validated + @Min/@NotNull 등이 파라미터에서 터질 때 발생
       =========================== */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MessageResponse<?>> handleConstraint(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MessageResponse.fail(ErrorCode.ERR_NOT_VALID_VALUE.name(), ErrorCode.ERR_NOT_VALID_VALUE.message()));
    }

    /* ===========================
       400 Bad Request - 타입 오류
       - PathVariable/RequestParam 타입 변환 실패 (Long에 "abc" 등)
       =========================== */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MessageResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MessageResponse.fail(ErrorCode.ERR_INVALID_PARAMETER.name(), ErrorCode.ERR_INVALID_PARAMETER.message()));
    }

    /* ===========================
       400 Bad Request - 필수 파라미터 누락
       - required request param 누락 시 발생
       =========================== */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<MessageResponse<?>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MessageResponse.fail(ErrorCode.ERR_INVALID_REQUEST.name(), ErrorCode.ERR_INVALID_REQUEST.message()));
    }

    /* ===========================
       400 Bad Request - JSON 파싱 오류
       - 잘못된 JSON, enum 변환 실패 등
       =========================== */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MessageResponse<?>> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MessageResponse.fail(ErrorCode.ERR_JSON_PARSE_ERROR.name(), ErrorCode.ERR_JSON_PARSE_ERROR.message()));
    }

    /* ===========================
       409 Conflict - DB 무결성/제약조건 위반
       - Unique/FK/NotNull 제약 등
       =========================== */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MessageResponse<?>> handleIntegrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(MessageResponse.fail(ErrorCode.ERR_DATA_INTEGRITY_VIOLATION.name(), ErrorCode.ERR_DATA_INTEGRITY_VIOLATION.message()));
    }

    /* ===========================
       2) 외부 연동 예외 (PortOne)
       - PortOneApiException 클래스는 유지 (외부 오류 정보 보존)
       - 응답 포맷만 MessageResponse로 처리
       =========================== */
    @ExceptionHandler(PortOneApiException.class)
    public ResponseEntity<MessageResponse<?>> handlePortOne(PortOneApiException e) {
        String msg = String.format("PortOne 오류(%s): %s", e.getErrorCode(), e.getErrorMessage());
        return ResponseEntity.status(e.getHttpStatus())
                .body(MessageResponse.fail(ErrorCode.ERR_PORTONE_API_FAILED.name(), msg));
    }

    /* ===========================
       500 Internal Server Error
       - 처리되지 않은 모든 예외
       =========================== */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse<?>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MessageResponse.fail(ErrorCode.ERR_INTERNAL_SERVER.name(), ErrorCode.ERR_INTERNAL_SERVER.message()));
    }
}

