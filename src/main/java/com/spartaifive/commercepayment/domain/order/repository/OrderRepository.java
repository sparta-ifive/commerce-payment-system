package com.spartaifive.commercepayment.domain.order.repository;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
