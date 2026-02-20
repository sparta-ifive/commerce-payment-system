package com.spartaifive.commercepayment.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AddOrderRequest {
    @Valid
    @NotNull(message = "주문 상품 목록은 필수 입니다")
    private List<RequestProduct> orderProducts;

    @Getter
    @AllArgsConstructor
    public static class RequestProduct {
        @NotNull(message = "productId는 필수 입니다")
        private Long productId;
        @Min(value = 0, message = "주문 양은 음수일 수 없습니다")
        @NotNull(message = "주문 양은 필수 입니다")
        private Long quantity;
    }
}
