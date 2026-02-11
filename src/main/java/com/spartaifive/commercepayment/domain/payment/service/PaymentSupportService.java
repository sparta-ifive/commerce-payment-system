package com.spartaifive.commercepayment.domain.payment.service;

import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.entity.OrderStatus;
import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.payment.dto.PaymentOrderEntities;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
import com.spartaifive.commercepayment.domain.payment.repository.PaymentRepository;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;
import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentSupportService {
    private final PaymentRepository paymentRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean shouldDoPayment(String portonePaymentId, boolean fromWebHook) {
        if (fromWebHook) {
            int count = paymentRepository.confirmWebhook(portonePaymentId);
            if(count > 0) {
                Optional<Payment> payment = paymentRepository.getClientConfirmedPayment(portonePaymentId);
                return payment.isPresent();
            }
        }else {
            int count = paymentRepository.confirmClient(portonePaymentId);
            if(count > 0) {
                Optional<Payment> payment = paymentRepository.getWebhookConfirmedPayment(portonePaymentId);
                return payment.isPresent();
            }
        }

        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPayment(String portonePaymentId) {
        PaymentOrderEntities entities =
                getPaymentOrderEntities(portonePaymentId);
        checkPaymentValid(entities);
        finalizePayment(entities);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPaymentAsFail(String portonePaymentId) {
        Payment paymentToFail = paymentRepository.findByPaymentId(portonePaymentId)
                .orElseThrow(() -> new RuntimeException("주문을 찾지 못했습니다"));
        paymentToFail.updateStatus(PaymentStatus.FAILED);
        paymentRepository.save(paymentToFail);
    }

    public PaymentOrderEntities getPaymentOrderEntities(String portonePaymentId) {
        Payment payment = paymentRepository.findByPaymentId(portonePaymentId).orElseThrow(
                ()->new RuntimeException(String.format("%s id의 결제를 발견하지 못했습니다", portonePaymentId)));

        Order order = payment.getOrder();

        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder_Id(order.getId());

        List<Long> productIds = orderProducts.stream().map((x)->x.getProduct().getId()).toList();

        List<Product> products = productRepository.findAllByStatusAndId(ProductStatus.ON_SALE, productIds);

        return new PaymentOrderEntities(payment, order, orderProducts, products);
    }

    private void checkPaymentValid(
            PaymentOrderEntities entities
    ) {
        // TODO: 가격이 맞는지 확인

        if (entities.getOrder().getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new RuntimeException("주문 상태가 대기 상태가 아닙니다");
        }

        if (entities.getOrderProducts().size() != entities.getProducts().size()) {
            throw new RuntimeException("주문의 갯수와 실제 주문 가능한 상품의 갯수가 다릅니다.");
        }
    }

    private void finalizePayment(PaymentOrderEntities entities) {
        // payment 상태 변경
        entities.getPayment().updateStatus(PaymentStatus.PAID);
        // 주문 상태 변경
        entities.getOrder().setStatusToCompleted();

        HashMap<Long, Product> idToProduct = new HashMap<>();

        for (final Product p : entities.getProducts()) {
            idToProduct.put(p.getId(), p);
        }

        for (final OrderProduct op : entities.getOrderProducts()) {
            Product p = idToProduct.get(op.getProduct().getId());
            p.decreaseStock(op.getQuantity());
        }

        paymentRepository.save(entities.getPayment());
        orderRepository.save(entities.getOrder());
        productRepository.saveAll(entities.getProducts());
    }
}
