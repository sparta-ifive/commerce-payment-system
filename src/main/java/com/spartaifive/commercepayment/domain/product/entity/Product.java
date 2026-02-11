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

    public Long decreaseStock(Long amount) {
        Long newStock = this.stock - amount;

        if (newStock < 0) {
            throw new RuntimeException(String.format("재고를 %s에서 %s 음수로 변경 시도", this.stock, newStock));
        }

        this.stock = newStock;

        if (this.stock <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }

        return this.stock;
    }
}
