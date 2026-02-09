package com.spartaifive.commercepayment.domain.product.repository;

import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import com.spartaifive.commercepayment.domain.product.entity.Product;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Long> {
    @Query("SELECT p FROM Product p where p.status = :status and p.id in :id")
    List<Product> findAllByStatusAndId(ProductStatus status, Iterable<Long> id);
}
