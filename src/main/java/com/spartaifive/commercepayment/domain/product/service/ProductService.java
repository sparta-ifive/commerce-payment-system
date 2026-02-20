package com.spartaifive.commercepayment.domain.product.service;

import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import com.spartaifive.commercepayment.domain.product.dto.response.GetManyProductsResponse;
import com.spartaifive.commercepayment.domain.product.dto.response.GetProductResponse;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;
import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.spartaifive.commercepayment.common.exception.ErrorCode.ERR_PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public GetProductResponse getProduct(Long productId) {
        // TODO: 이에 관련 에러 생성
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new ServiceErrorException(ERR_PRODUCT_NOT_FOUND)
        );

        return GetProductResponse.of(product);
    }

    @Transactional(readOnly = true)
    public List<GetManyProductsResponse> getManyProducts() {
        List<Product> products = productRepository.findAllByStatusIn(
                List.of(ProductStatus.ON_SALE, ProductStatus.OUT_OF_STOCK));

        List<GetManyProductsResponse> dtos = new ArrayList<>();

        for (final Product p : products) {
            dtos.add(GetManyProductsResponse.of(p));
        }

        return dtos;
    }
}
