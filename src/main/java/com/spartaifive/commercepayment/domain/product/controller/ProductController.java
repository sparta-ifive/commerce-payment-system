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

    // TODO: 저희 설계에 따르면 상품 조회 API는 누구나 조회 할 수 있는 것이 맞습니다.
    //
    // 하지만 현재 SecurityConfig를 보면 /api/public 아래에 있는 api들만
    // 공개적을 들어가도 록 설정 되어 있습니다.
    //
    // 그렇다면 저희에게 옵션은 두가지 입니다.
    //
    // 1. 공개 api를 public 아래로 둔다
    //  그렇게 된다면 /api/products/{productId} 는
    //  /api/public/products/{productId}가 되겠죠.
    //
    // 2. api 주소는 그대로 두고 SecurityConfig를 수정한다.
    //
    // 이에 대해서 저혼자 결정 할 수 있는 문제가 아니므로 일단은 
    // login을 해야 상품 조회가 가능합니다.
    // 이에 대해서는 추가로 팀원들과의 논의가 필요 할 듯 합니다.

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
