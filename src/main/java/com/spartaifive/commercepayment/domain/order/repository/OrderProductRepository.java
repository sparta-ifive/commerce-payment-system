package com.spartaifive.commercepayment.domain.order.repository;

import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
}

