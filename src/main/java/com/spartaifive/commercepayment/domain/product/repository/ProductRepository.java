package com.spartaifive.commercepayment.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spartaifive.commercepayment.domain.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product,Long> {
}
