package com.spartaifive.commercepayment.domain.payment.repository;

import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByPaymentId(String paymentId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Payment p SET p.webhookConfirmed = true where p.paymentId = :paymentId AND p.webhookConfirmed = false")
    int confirmWebhook(@Param("paymentId") String paymentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Payment p SET p.clientConfirmed = true where p.paymentId = :paymentId AND p.clientConfirmed = false")
    int confirmClient(@Param("paymentId") String paymentId);

    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId and p.clientConfirmed = true")
    Optional<Payment> getClientConfirmedPayment(@Param("paymentId") String paymentId);

    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId and p.webhookConfirmed = true")
    Optional<Payment> getWebhookConfirmedPayment(@Param("paymentId") String paymentId);
}
