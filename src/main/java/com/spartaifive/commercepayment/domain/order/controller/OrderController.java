package com.spartaifive.commercepayment.domain.order.controller;

import com.spartaifive.commercepayment.common.auth.AuthUtil;
import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.order.dto.request.AddOrderRequest;
import com.spartaifive.commercepayment.domain.order.dto.response.GetManyOrdersResponse;
import com.spartaifive.commercepayment.domain.order.dto.response.GetOrderResponse;
import com.spartaifive.commercepayment.domain.order.service.OrderService;
import com.spartaifive.commercepayment.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class OrderController {
    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping("/api/orders")
    public ResponseEntity<DataResponse<GetOrderResponse>> addOrder(
            @Valid @RequestBody AddOrderRequest req
    ) {
        Long userId = AuthUtil.getCurrentUserId();
        GetOrderResponse res = orderService.addOrder(req, userId);

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
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

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DataResponse.success(String.valueOf(HttpStatus.OK.value()), res));
    }

    @GetMapping("/api/orders")
    public ResponseEntity<DataResponse<List<GetManyOrdersResponse>>> getManyOrders() {
        Long userId = AuthUtil.getCurrentUserId();
        List<GetManyOrdersResponse> res = orderService.getManyOrders(userId);

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(DataResponse.success(String.valueOf(HttpStatus.OK.value()), res));
    }
}
