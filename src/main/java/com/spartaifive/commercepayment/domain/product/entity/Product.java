package com.spartaifive.commercepayment.domain.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    // TODO: 팀원들과 BigDecimal로의 변경을 상의
    @NotNull
    @Column(nullable = false)
    @Min(0)
    private Long price;

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
            Long price,
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
}
