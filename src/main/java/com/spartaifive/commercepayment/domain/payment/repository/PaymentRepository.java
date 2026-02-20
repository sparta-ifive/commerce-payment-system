package com.spartaifive.commercepayment.domain.payment.repository;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // merchantPaymentId로 결제 조회 (동시성 잠금)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findByMerchantPaymentId(String merchantPaymentId);

    // portOnePaymentId로 결제 조회 (동시성 잠금)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findByPortonePaymentId(String portonePaymentId);

    // MerchantId로 가장 마지막에 저장된 결제 목록 조회
    Optional<Payment> findTopByMerchantPaymentIdOrderByIdDesc(String merchantPaymentId);

    // default 메서드로 JPA 메서드 감싸기
    default Optional<Payment> findLatestByMerchantPaymentId(String merchantPaymentId) {
        return findTopByMerchantPaymentIdOrderByIdDesc(merchantPaymentId);
    }

    List<Payment> findByUserId(Long userId);
}
