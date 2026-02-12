package com.spartaifive.commercepayment.domain.order.customexception;

public class EmptyOrderException extends RuntimeException {
    public EmptyOrderException() {
        super("선택된 상품이 없습니다.");
    }
}
