package com.spartaifive.commercepayment.domain.order.entity;

import com.spartaifive.commercepayment.domain.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "order_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String productName;

    @NotNull
    @Min(0)
    // 이렇게 된다면 9,999,999,999,999.99 원이 저희 쇼핑몰의 최대 금액이 됩니다.
    @Column(precision = 15, scale = 2, nullable = false) 
    private BigDecimal productPrice;

    @NotNull
    @Column(nullable = false)
    @Min(0)
    private Long quantity;

    // NOTE: 저희는 주문 삭제는 고려하지 않음으로 nullable = false
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "order_id")
    private Order order;

    // NOTE: 저희는 상품 삭제는 고려하지 않음으로 nullable = false
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "product_id")
    private Product product;

    public OrderProduct (
        Order order,
        Product product,
        Long quantity
    ) {
        this.order = order;

        this.product = product;
        this.productPrice = product.getPrice();
        this.productName = product.getName();

        this.quantity = quantity;
    }
}
