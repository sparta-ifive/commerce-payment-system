package com.spartaifive.commercepayment.domain.payment.repository;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPortonePaymentId(String portonePaymentId);
    List<Payment> findAllByOrder_id(Long orderId);
    Optional<Payment> findTopByOrder_IdAndPaymentStatusOrderByIdDesc(
            Long orderId,
            PaymentStatus paymentStatus
    );
    Optional<Payment> findTopByMerchantPaymentIdAndPaymentStatusOrderByIdDesc(
            String merchantPaymentId,
            PaymentStatus paymentStatus
    );

    // default 메서드로 JPA 메서드 감싸기
    default Optional<Payment> findLatestReadyByOrderId(Long orderId) {
        return findTopByOrder_IdAndPaymentStatusOrderByIdDesc(orderId, PaymentStatus.READY);
    }

    default Optional<Payment> findLatestReadyByMerchantPaymentId(String merchantPaymentId) {
        return findTopByMerchantPaymentIdAndPaymentStatusOrderByIdDesc(merchantPaymentId, PaymentStatus.READY);
    }
}
