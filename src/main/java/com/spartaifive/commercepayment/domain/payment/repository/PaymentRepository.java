package com.spartaifive.commercepayment.domain.payment.repository;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
