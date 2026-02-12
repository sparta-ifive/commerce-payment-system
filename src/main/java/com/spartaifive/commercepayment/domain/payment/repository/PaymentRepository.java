package com.spartaifive.commercepayment.domain.payment.repository;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // merchantPaymentId로 결제 조회
    Optional<Payment> findByMerchantPaymentId(String merchantPaymentId);
    List<Payment> findAllByOrder_id(Long orderId);
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
    default Optional<Payment> findLatestReadyByOrderId(Long orderId) {
        return findTopByOrder_IdAndPaymentStatusOrderByIdDesc(orderId, PaymentStatus.READY);
    }

    default Optional<Payment> findLatestReadyByMerchantPaymentId(String merchantPaymentId) {
        return findTopByMerchantPaymentIdAndPaymentStatusOrderByIdDesc(merchantPaymentId, PaymentStatus.READY);
    }

    default Optional<Payment> findLatestByOrderId(Long orderId) {
        return findTopByOrder_IdOrderByIdDesc(orderId);
    }

    default Optional<Payment> findLatestPaidByOrderId(Long orderId) {
        return findTopByOrder_IdAndPaymentStatusOrderByIdDesc(orderId, PaymentStatus.PAID);
    }
}
