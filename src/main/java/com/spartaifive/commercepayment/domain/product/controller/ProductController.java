package com.spartaifive.commercepayment.domain.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.spartaifive.commercepayment.common.response.DataResponse;
import com.spartaifive.commercepayment.domain.product.dto.GetManyProductsResponse;
import com.spartaifive.commercepayment.domain.product.dto.GetProductResponse;
import com.spartaifive.commercepayment.domain.product.service.ProductService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class ProductController {
    private final ProductService productService;

    @GetMapping("/api/products/{productId}")
    public ResponseEntity<DataResponse<GetProductResponse>> getProduct(
            @PathVariable Long productId
    ) {
        GetProductResponse res = productService.getProduct(productId);

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(DataResponse.success("SUCCESS", res));
    }

    @GetMapping("/api/products")
    public ResponseEntity<DataResponse<List<GetManyProductsResponse>>> getManyProducts() {
        List<GetManyProductsResponse> res = productService.getManyProducts();

        // TODO: 무슨 코드를 넣을지 잘 모르겠네요
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(DataResponse.success("SUCCESS", res));
    }
}
