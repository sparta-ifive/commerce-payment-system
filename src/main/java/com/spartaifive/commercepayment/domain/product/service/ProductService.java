package com.spartaifive.commercepayment.domain.product.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spartaifive.commercepayment.domain.product.dto.GetManyProductsResponse;
import com.spartaifive.commercepayment.domain.product.dto.GetProductResponse;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public GetProductResponse getProduct(Long productId) {
        // TODO: 이에 관련 에러 생성
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new RuntimeException("상품을 찾을 수 없습니다.")
        );

        return GetProductResponse.of(product);
    }

    @Transactional(readOnly = true)
    public List<GetManyProductsResponse> getManyProducts() {
        List<Product> products = productRepository.findAll();

        List<GetManyProductsResponse> dtos = new ArrayList<>();

        for (final Product p : products) {
            dtos.add(GetManyProductsResponse.of(p));
        }

        return dtos;
    }
}
