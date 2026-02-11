//
//package com.spartaifive.commercepayment.domain.webhookevent.service;
//
//import com.spartaifive.commercepayment.domain.order.entity.Order;
//import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
//import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
//import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
//import com.spartaifive.commercepayment.domain.payment.entity.Payment;
//import com.spartaifive.commercepayment.domain.payment.repository.PaymentRepository;
//import com.spartaifive.commercepayment.domain.product.entity.Product;
//import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;
//import com.spartaifive.commercepayment.domain.webhookevent.dto.WebhookDto;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@RequiredArgsConstructor
//@Service
//@Slf4j
//public class WebhookDataChangeService {
//
//    private final ProductRepository productRepository;
//    private final PaymentRepository paymentRepository;
//    private final OrderRepository orderRepository;
//    private final OrderProductRepository orderProductRepository;
//
//    public void changeStock(WebhookDto.RequestWebhook webhookDto) {
//
//        //주문상품 데이터 찾기
//        String paymentId = webhookDto.getPaymentId();
//        Payment payment = paymentRepository.findByPaymentId(paymentId).orElseThrow(() -> new IllegalStateException("해당 결제가 없습니다."));
//        Order order = orderRepository.findById(payment.getOrder().getId()).orElseThrow(() -> new IllegalStateException("해당 주문이 없습니다."));
//        List<OrderProduct> productList = orderProductRepository.findAllByOrder_Id(order.getId());
//
//        //productList의 product 재고 차감
//        for(int i = 0; i< productList.size(); i++) {
//            Product product = productRepository.findById(productList.get(i).getProduct().getId()).orElseThrow(() -> new IllegalStateException("해당 상품이 없습니다."));
//            product.decreaseStock(productList.get(i).getQuantity());
//        }
//    }
//
//}
//
