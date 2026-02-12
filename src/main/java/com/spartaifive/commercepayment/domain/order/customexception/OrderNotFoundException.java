package com.spartaifive.commercepayment.domain.order.customexception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(Long orderId) {
        super(String.format("이 %s id의 주문을 찾을 수 없습니다.", orderId));
    }
}
