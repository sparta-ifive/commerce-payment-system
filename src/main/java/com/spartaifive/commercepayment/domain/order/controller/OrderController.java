package com.spartaifive.commercepayment.domain.order.controller;

import com.spartaifive.commercepayment.common.auth.AuthUtil;
import com.spartaifive.commercepayment.common.auth.UserDetailsImpl;
import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.order.dto.AddOrderRequest;
import com.spartaifive.commercepayment.domain.order.dto.GetManyOrdersResponse;
import com.spartaifive.commercepayment.domain.order.dto.GetOrderResponse;
import com.spartaifive.commercepayment.domain.order.service.OrderService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/api/orders")
    public ResponseEntity<DataResponse<GetOrderResponse>> addOrder(
            @Valid @RequestBody AddOrderRequest req
    ) {
        Long userId = AuthUtil.getCurrentUserId();
        GetOrderResponse res = orderService.addOrder(req, userId);

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(DataResponse.success("SUCCESS", res));
    }

    @GetMapping("/api/orders/{orderId}")
    public ResponseEntity<DataResponse<GetOrderResponse>> getOrder(
            @PathVariable Long orderId
    ) {
        Long userId = AuthUtil.getCurrentUserId();
        GetOrderResponse res = orderService.getOrder(orderId, userId);

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DataResponse.success("SUCCESS", res));
    }

    @GetMapping("/api/orders")
    public ResponseEntity<DataResponse<List<GetManyOrdersResponse>>> getManyOrders() {
        Long userId = AuthUtil.getCurrentUserId();
        List<GetManyOrdersResponse> res = orderService.getManyOrders(userId);

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DataResponse.success("SUCCESS", res));
    }
}
