package com.spartaifive.commercepayment.domain.order.controller;

import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.order.dto.AddOrderRequest;
import com.spartaifive.commercepayment.domain.order.dto.GetManyOrdersResponse;
import com.spartaifive.commercepayment.domain.order.dto.GetOrderResponse;
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

    // TODO: 여기에는 유저의 ID가 원래 넘겨져야 합니다.
    // 하지만 아직 유저 인증이 구현되지 않았으므로 일단은 skip
    @PostMapping("/api/orders")
    public ResponseEntity<DataResponse<GetOrderResponse>> addOrder(
            @Valid @RequestBody AddOrderRequest req
    ) {
        GetOrderResponse res = orderService.addOrder(req);

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(DataResponse.success("SUCCESS", res));
    }

    // TODO: 여기에는 유저의 ID가 원래 넘겨져야 합니다.
    // 하지만 아직 유저 인증이 구현되지 않았으므로 일단은 skip
    @GetMapping("/api/orders/{orderId}")
    public ResponseEntity<DataResponse<GetOrderResponse>> getOrder(
            @PathVariable Long orderId
    ) {
        GetOrderResponse res = orderService.getOrder(orderId);

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DataResponse.success("SUCCESS", res));
    }

    // TODO: 여기에는 유저의 ID가 원래 넘겨져야 합니다.
    // 하지만 아직 유저 인증이 구현되지 않았으므로 일단은 skip
    @GetMapping("/api/orders")
    public ResponseEntity<DataResponse<List<GetManyOrdersResponse>>> getManyOrders() {
        List<GetManyOrdersResponse> res = orderService.getManyOrders();

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DataResponse.success("SUCCESS", res));
    }
}
