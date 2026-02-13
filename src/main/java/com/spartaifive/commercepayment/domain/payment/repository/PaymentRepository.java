package com.spartaifive.commercepayment.domain.payment.repository;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // merchantPaymentId로 결제 조회 (동시성 잠금)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.merchantPaymentId = :merchantPaymentId")
    Optional<Payment> findByMerchantPaymentIdForUpdate(String merchantPaymentId);
    // orderId로 결제 목록에서 가장 마지막에 저장된 status가 ready인 결제 조회
    Optional<Payment> findTopByOrder_IdAndPaymentStatusOrderByIdDesc(
            Long orderId,
            PaymentStatus paymentStatus
    );
    // merchantId로 결제 목록에서 가장 마지막에 저장된 status가 ready인 결제 조회
    Optional<Payment> findTopByMerchantPaymentIdAndPaymentStatusOrderByIdDesc(
            String merchantPaymentId,
            PaymentStatus paymentStatus
    );
    // orderId로 결제 목록에서 가장 마지막에 저장된 결제 조회
    Optional<Payment> findTopByOrder_IdOrderByIdDesc(Long orderId);

    // default 메서드로 JPA 메서드 감싸기
    default Optional<Payment> findLatestReadyByMerchantPaymentId(String merchantPaymentId) {
        return findTopByMerchantPaymentIdAndPaymentStatusOrderByIdDesc(merchantPaymentId, PaymentStatus.READY);
    }

    default Optional<Payment> findLatestByOrderId(Long orderId) {
        return findTopByOrder_IdOrderByIdDesc(orderId);
    }
}
