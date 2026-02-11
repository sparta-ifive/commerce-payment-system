package com.spartaifive.commercepayment.domain.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Min(0)
    // 이렇게 된다면 99,999,999.99 원이 저희 쇼핑몰의 최대 금액이 됩니다.
    @Column(precision = 10, scale = 2, nullable = false) 
    private BigDecimal price;

    @NotNull
    @Column(nullable = false)
    @Min(0)
    private Long stock;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @NotNull
    @Column(nullable = false)
    @Size(max = 2048)
    private String description;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column()
    private LocalDateTime modifiedAt;

    public Product(
            String name,
            BigDecimal price,
            Long stock,
            ProductStatus status,
            ProductCategory category,
            String description
    ) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.category = category;
        this.description = description;
    }

    public void decreaseStock(Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("차감 수량은 0보다 커야합니다");
        }
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다 stock=" + this.stock + ", quantity=" + quantity);
        }
        this.stock -= quantity;
    }

    public void increaseStock(Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("증가 수량은 0보다 커야합니다");
        }
        this.stock += quantity;
    }
}
