package com.spartaifive.commercepayment.domain.order.repository;

import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    List<OrderProduct> findAllByOrder_Id(Long orderId);
}

