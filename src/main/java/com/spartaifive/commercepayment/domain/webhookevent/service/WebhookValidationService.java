//package com.spartaifive.commercepayment.domain.webhookevent.service;
//
//import com.spartaifive.commercepayment.common.external.portone.PortOnePaymentResponse;
//import com.spartaifive.commercepayment.domain.order.entity.Order;
//import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
//import com.spartaifive.commercepayment.domain.order.entity.OrderStatus;
//import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
//import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
//import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
//import com.spartaifive.commercepayment.domain.webhookevent.dto.WebhookDto;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@RequiredArgsConstructor
//@Service
//@Slf4j
//public class WebhookValidationService {
//
//    private final OrderProductRepository orderProductRepository;
//    private final OrderRepository orderRepository;
//    private final PaymentRepository paymentRepository;
//
//    @Transactional
//    public void validate(WebhookDto.RequestWebhook webhookDto, PortOnePaymentResponse paymentResponse) {
//
//        String webhookId = webhookDto.getWebhookId();
//        String paymentId = webhookDto.getPaymentId();
//        LocalDateTime receivedAt = webhookDto.getReceivedAt();
//
//
//        //내부 데이터 정합성 확인 시작
//            /*
//            * 참고 웹훅 발생
//            * 결제가 승인되었을 때(모든 결제 수단) - (status : paid)
//              가상계좌가 발급되었을 때 - (status : ready)
//              가상계좌에 결제 금액이 입금되었을 때 - (status : paid)
//              예약결제가 시도되었을 때 - (status : paid or failed)
//              관리자 콘솔에서 결제 취소되었을 때 - (status : cancelled)
//              결제 실패시에는 웹훅 X
//            * */
//
//        Payment payment = paymentRepository.findByPortonePaymentId(paymentId).orElseThrow(() -> new IllegalStateException("해당 결제가 없습니다."));
//
//        Order order = orderRepository.findById(payment.getOrder().getId()).orElseThrow(() -> new IllegalStateException("해당 주문이 없습니다."));
//
//        //Todo: 환불 구현 후 확인
//        // 환불 상태라면 환불에 데이터 저장 잘 되었는지
////!        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
////!             Refund refund = refundRepository.findByPaymentPkId(payment.getId()).orElseThrow(() -> new IllegalStateException("환불 이력이 없습니다."));
////!         }
//
//
//        //결제 승인 상태라면 주문과 결제 둘다 정상 상태인지
//
//        if(payment.getPaymentStatus() == PaymentStatus.PAID) {
//            if(!(order.getStatus() == OrderStatus.COMPLETED)) {
//                throw new IllegalStateException("주문과 결제의 상태가 일치하지 않습니다.");
//            }
//        }
//        if(!(payment.getPaymentStatus() == PaymentStatus.PAID)) {
//            if(order.getStatus() == OrderStatus.COMPLETED) {
//                throw new IllegalStateException("주문과 결제의 상태가 일치하지 않습니다.");
//            }
//        }
//
//        //총 결제금액 계산
//        List<OrderProduct> products = orderProductRepository.findAllByOrder_Id(order.getId());
//        BigDecimal calculatedPrice = BigDecimal.valueOf(0);
//        for (OrderProduct product: products) {
//            calculatedPrice = calculatedPrice.add(product.getProductPrice());
//        }
//        // 결제금액 일치 확인
//        if (payment.getActualAmount().compareTo(payment.getExpectedAmount()) != 0) {
//            throw new IllegalStateException("결제 스냅샷과 결제 금액이 일치하지 않습니다.");
//        }
//
//        if (payment.getActualAmount().compareTo(calculatedPrice) != 0) {
//            throw new IllegalStateException("결제 스냅샷과 서버가 계산한 결제 금액이 일치하지 않습니다.");
//        }
//        if (payment.getActualAmount().compareTo(order.getTotalPrice()) != 0) {
//            throw new IllegalStateException("결제 스냅샷과 주문에서 확인한 결제 금액이 일치하지 않습니다.");
//        }
//        if(payment.getActualAmount().compareTo(paymentResponse.amount().total()) != 0) {
//            throw new IllegalStateException(" 결제 스냅샷과 포트원에서 확인한 결제 금액이 일치하지 않습니다.");
//        }
//
//    }
//
//    public void updatePaymentConfirmed() {
//
//    }
//}
//
