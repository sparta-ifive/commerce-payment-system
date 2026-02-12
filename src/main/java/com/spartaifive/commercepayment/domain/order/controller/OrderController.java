package com.spartaifive.commercepayment.domain.order.controller;

import com.spartaifive.commercepayment.common.auth.AuthUtil;
import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.order.dto.request.AddOrderRequest;
import com.spartaifive.commercepayment.domain.order.dto.response.GetManyOrdersResponse;
import com.spartaifive.commercepayment.domain.order.dto.response.GetOrderResponse;
import com.spartaifive.commercepayment.domain.order.service.OrderService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(DataResponse.success(
                        String.valueOf(HttpStatus.CREATED.value()), 
                        res));
    }

    @GetMapping("/api/orders/{orderId}")
    public ResponseEntity<DataResponse<GetOrderResponse>> getOrder(
            @PathVariable Long orderId
    ) {
        Long userId = AuthUtil.getCurrentUserId();
        GetOrderResponse res = orderService.getOrder(orderId, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DataResponse.success(String.valueOf(HttpStatus.OK.value()), res));
    }

    @GetMapping("/api/orders")
    public ResponseEntity<DataResponse<List<GetManyOrdersResponse>>> getManyOrders() {
        Long userId = AuthUtil.getCurrentUserId();
        List<GetManyOrdersResponse> res = orderService.getManyOrders(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DataResponse.success(String.valueOf(HttpStatus.OK.value()), res));
    }
}
