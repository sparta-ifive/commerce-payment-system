package com.spartaifive.commercepayment.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // ===== 공통 =====
    ERR_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청이 올바르지 않습니다"),
    ERR_NOT_VALID_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 값입니다"),
    ERR_INTERNAL_SERVER(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다"),
    ERR_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "요청 파라미터 형식이 올바르지 않습니다"),
    ERR_JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "요청 본문(JSON) 형식이 올바르지 않습니다"),
    ERR_DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "데이터 무결성 제약을 위반했습니다"),

    // ===== 인증/인가(Security) =====
    ERR_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    ERR_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다"),
    ERR_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "토큰이 없습니다"),
    ERR_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),

    // ===== 유저(User) =====
    ERR_DUPLICATED_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),
    ERR_DUPLICATED_PHONE(HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다"),
    ERR_INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 올바르지 않습니다"),
    ERR_REFRESH_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 리프레시 토큰입니다"),
    ERR_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다"),
    ERR_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    ERR_REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "이미 로그아웃된 토큰입니다"),

    // ===== 상품(Product) =====
    ERR_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다"),
    ERR_PRODUCT_FAILED_TO_DECREASE_STOCK(HttpStatus.INTERNAL_SERVER_ERROR, "상품 재고 차감에 실패했습니다"),
    ERR_PRODUCT_FAILED_TO_INCREASE_STOCK(HttpStatus.INTERNAL_SERVER_ERROR, "상품 재고 증가에 실패했습니다"),

    // ===== 멤버십(Membership) =====
    ERR_MEMBERSHIP_GRADE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "기본 멤버십(NORMAL)을 찾을 수 없습니다"),

    // ===== 주문(Order) =====
    ERR_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다"),
    ERR_ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "주문에 대한 권한이 없습니다"),
    ERR_EMPTY_ORDER(HttpStatus.BAD_REQUEST, "주문 항목이 비어있습니다"),
    ERR_INVALID_ORDER_STATUS(HttpStatus.CONFLICT, "주문 상태가 올바르지 않습니다"),
    ERR_INVALID_ORDER_PRICE(HttpStatus.CONFLICT, "주문 금액이 올바르지 않습니다"),
    ERR_PRODUCTS_NOT_AVAILABLE(HttpStatus.CONFLICT, "구매할 수 없는 상품이 포함되어 있습니다"),
    ERR_NOT_ENOUGH_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다"),

    // ===== 결제(Payment) =====
    ERR_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제를 찾을 수 없습니다"),
    ERR_PAYMENT_NOT_READY(HttpStatus.CONFLICT, "결제 확정이 불가능한 상태입니다"),
    ERR_PAYMENT_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "결제 소유자가 아닙니다"),
    ERR_PAYMENT_ORDER_MISMATCH(HttpStatus.BAD_REQUEST, "주문 ID와 일치하지 않습니다"),
    ERR_PAYMENT_AMOUNT_MISMATCH(HttpStatus.CONFLICT, "결제 금액이 일치하지 않습니다"),
    ERR_PAYMENT_ALREADY_REFUNDED(HttpStatus.CONFLICT, "이미 환불된 결제입니다"),
    ERR_ORDER_ALREADY_REFUNDED(HttpStatus.CONFLICT, "환불된 주문은 재결제 할 수 없습니다"),
    ERR_PORTONE_PAYMENT_ID_MISMATCH(HttpStatus.CONFLICT, "이미 다른 portonePaymentId로 처리된 결제입니다"),
    ERR_PAYMENT_STATUS_TRANSITION_INVALID(HttpStatus.CONFLICT, "결제 상태 전이가 불가능합니다"),
    ERR_PAYMENT_AMOUNT_TOO_LOW(HttpStatus.BAD_REQUEST, "결제 금액이 최소 결제 금액 보다 적습니다"),

    // ===== 포인트 (Point) =====
    ERR_POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "포인트를 찾을 수 없습니다"),
    ERR_POINT_FAILED_TO_UPDATE_POINT_AMOUNT(HttpStatus.INTERNAL_SERVER_ERROR, "포인트의 양을 업데이트 하는데 실패했습니다"),
    ERR_POINT_FAILED_TO_CREATE_POINT(HttpStatus.INTERNAL_SERVER_ERROR, "포인트 생성을 실패했습니다"),
    ERR_POINT_FAILED_TO_SPEND_POINT(HttpStatus.INTERNAL_SERVER_ERROR, "포인트를 쓰는데 실패했습니다"),
    ERR_POINT_INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "포인트의 양이 부족합니다"),
    ERR_POINT_POINT_EXCEEDS_PAYMENT(HttpStatus.BAD_REQUEST, "결제 금액보다 더 많은 포인트를 사용할려고 합니다"),
    ERR_POINT_FAILED_TO_CALCULATE_TOTAL(HttpStatus.BAD_REQUEST, "포인트 총합을 계산하는데 실패했습니다"),

    // ===== 환불(Refund) =====
    ERR_REFUND_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 환불 이력이 존재합니다"),
    ERR_REFUND_NOT_ALLOWED_ORDER_STATUS(HttpStatus.CONFLICT, "환불이 불가능한 주문 상태입니다"),
    ERR_REFUND_NOT_ALLOWED_PAYMENT_STATUS(HttpStatus.CONFLICT, "환불이 불가능한 결제 상태입니다"),
    ERR_REFUND_AMOUNT_UNKNOWN(HttpStatus.CONFLICT, "환불 금액을 확인할 수 없습니다"),
    ERR_REFUND_AMOUNT_MISMATCH(HttpStatus.CONFLICT, "환불 금액이 일치하지 않습니다"),
    ERR_REFUND_TIMEOUT(HttpStatus.BAD_REQUEST, "환불 가능 기간이 지났습니다"),

    // ===== 외부 연동(PortOne) =====
    ERR_PORTONE_RESPONSE_NULL(HttpStatus.BAD_GATEWAY, "PortOne 응답이 null 입니다"),
    ERR_PORTONE_PAYMENT_NOT_PAID(HttpStatus.CONFLICT, "PortOne 결제가 확정 상태가 아닙니다"),
    ERR_PORTONE_PAYMENT_NOT_READY(HttpStatus.CONFLICT, "PortOne 결제가 대기 상태가 아닙니다"),
    ERR_PORTONE_API_FAILED(HttpStatus.BAD_GATEWAY, "PortOne API 호출이 실패했습니다"),

    // ===== 웹훅(Webhook) =====
    ERR_WEBHOOK_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "웹훅 서명 검증에 실패했습니다"),
    ERR_WEBHOOK_STATE_MISMATCH(HttpStatus.CONFLICT, "주문과 결제의 상태가 일치하지 않습니다"),
    ERR_WEBHOOK_AMOUNT_MISMATCH(HttpStatus.CONFLICT, "결제 금액 정합성 검증에 실패했습니다"),
    ERR_WEBHOOK_PROCESS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "웹훅 처리 중 서버 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public String message() {
        return message;
    }
}
