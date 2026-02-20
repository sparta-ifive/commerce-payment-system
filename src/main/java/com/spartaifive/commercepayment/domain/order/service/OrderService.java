package com.spartaifive.commercepayment.domain.order.service;

import com.spartaifive.commercepayment.common.exception.ErrorCode;
import com.spartaifive.commercepayment.common.exception.ServiceDataErrorException;
import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import com.spartaifive.commercepayment.domain.order.dto.request.AddOrderRequest;
import com.spartaifive.commercepayment.domain.order.dto.response.GetManyOrdersResponse;
import com.spartaifive.commercepayment.domain.order.dto.response.GetOrderResponse;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.order.util.OrderSupport;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;
import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;
import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final UserRepository userRepository;

    // TODO: 현재 선택한 상품에 대해서 추가 할 때 
    // 예를들어 고객이 포카칩, 사과 이렇게 샀을 때
    // 사과만 단종이 되었을 경우 바로 에러를 내뿜습니다.
    // 이게 좋은건지는 모르겠습니다.

    @Transactional
    public GetOrderResponse addOrder(AddOrderRequest req, Long userId) {
        if (req.getOrderProducts().size() <= 0) {
            throw new ServiceErrorException(ErrorCode.ERR_EMPTY_ORDER);
        }

        // 주문 정규화
        req = OrderSupport.NormalizeAddOrderRequest(req);

        Map<Long, AddOrderRequest.RequestProduct> productIdToReq = new HashMap<>();
        for (AddOrderRequest.RequestProduct reqP : req.getOrderProducts()) {
            Long id = reqP.getProductId();
            if (id != null) {
                productIdToReq.put(id, reqP);
            }
        }

        // 요청에서 들어온 상품 들을 조회
        // 이때 단종 이나 품절 상품은 제외
        List<Product> products = productRepository.findAllByStatusAndId(
                ProductStatus.ON_SALE, productIdToReq.keySet());

        // 주문 상품의 갯수와 실제 상품의 갯수가 다를 경우 에러 반환
        if (products.size() > req.getOrderProducts().size()) {
            throw new ServiceErrorException(
                    ErrorCode.ERR_INTERNAL_SERVER,
                    "데이터베이스에서 예상보다 많은 상품이 조회되었습니다. 시스템 관리자에게 문의하세요");
        }

        if (products.size() < req.getOrderProducts().size()) {
            for (Product p : products) {
                productIdToReq.remove(p.getId());
            }
            throw new ServiceDataErrorException(
                    ErrorCode.ERR_PRODUCTS_NOT_AVAILABLE,
                    List.copyOf(productIdToReq.keySet())
            );
        }

        // 주문 시도 양보다 재고가 적으면 에러 반환
        for (final Product p : products) {
            Long quantity = productIdToReq.get(p.getId()).getQuantity();
            if (p.getStock() < quantity) {
                throw new ServiceErrorException(
                        ErrorCode.ERR_NOT_ENOUGH_STOCK);
            }
        }

        // 총 금액을 계산
        BigDecimal total = BigDecimal.ZERO;

        for (final Product p : products) {
            Long quantity = productIdToReq.get(p.getId()).getQuantity();

            if (quantity != null) {
                total = total.add(p.getPrice().multiply(new BigDecimal(quantity)));
            }
        }

        User userRef = userRepository.getReferenceById(userId);

        // 주문 객체 생성
        Order order = new Order(total, userRef);

        // 주문 상품 생성
        List<OrderProduct> orderProducts = new ArrayList<>();

        for (final Product p : products) {
            Long quantity = productIdToReq.get(p.getId()).getQuantity();

            if (quantity != null) {
                OrderProduct orderProduct = new OrderProduct(
                    order, p, quantity
                );
                orderProducts.add(orderProduct);
            }
        }

        order = orderRepository.saveAndFlush(order);
        orderProducts = orderProductRepository.saveAllAndFlush(orderProducts);

        return GetOrderResponse.fromOrderAndOrderProducts(
                order,
                orderProducts);
    }


    @Transactional(readOnly = true)
    public GetOrderResponse getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(()->
                new ServiceErrorException(ErrorCode.ERR_ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new ServiceErrorException(ErrorCode.ERR_ORDER_ACCESS_DENIED);
        }

        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder_Id(order.getId());

        return GetOrderResponse.fromOrderAndOrderProducts(
                order,
                orderProducts);
    }

    @Transactional(readOnly = true)
    public List<GetManyOrdersResponse> getManyOrders(Long userId) {
        List<Order> orders = orderRepository.findAllByUserId(userId);

        List<GetManyOrdersResponse> dtos = new ArrayList<>();

        for (final Order order : orders) {
            dtos.add(GetManyOrdersResponse.of(order));
        }

        return dtos;
    }
}
