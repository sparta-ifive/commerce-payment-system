package com.spartaifive.commercepayment.domain.refund.repository;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.refund.entity.Refund;
import com.spartaifive.commercepayment.domain.refund.entity.RefundStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Refund> findByPayment(Payment payment);
}
