package com.spartaifive.commercepayment.common.initializer;

import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;
import com.spartaifive.commercepayment.domain.product.entity.ProductCategory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component()
@ConditionalOnProperty(
    name = "app.add-test-products",
    havingValue = "true",
    matchIfMissing = false
)
@RequiredArgsConstructor
public class ProductDataInitializer implements ApplicationRunner {
    private final ProductRepository productRepository;

    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        List<Product> testProducts = List.of(
                new Product("포카칩", new BigDecimal("1000"), 10L, ProductStatus.ON_SALE, ProductCategory.FOOD, "맛있는 감자칩"),
                new Product("배터리", new BigDecimal("2000"), 1L, ProductStatus.ON_SALE, ProductCategory.ELECTRONICS, "AA 배터리"),
                new Product("요요", new BigDecimal("1500"), 5L, ProductStatus.DISCONTINUED ,ProductCategory.TOY, "재밌는 요요"),
                new Product("모자", new BigDecimal("1500"), 3L, ProductStatus.ON_SALE, ProductCategory.CLOTHES, "그냥 모자"),
                new Product("레고", new BigDecimal("5000"), 12L, ProductStatus.ON_SALE, ProductCategory.TOY, "재밌는 레고")
        );

        productRepository.saveAll(testProducts);
    }
}
