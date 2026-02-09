package com.spartaifive.commercepayment.domain.order.service;

import com.spartaifive.commercepayment.domain.order.dto.AddOrderRequest;
import com.spartaifive.commercepayment.domain.order.dto.GetManyOrdersResponse;
import com.spartaifive.commercepayment.domain.order.dto.GetOrderResponse;
import com.spartaifive.commercepayment.domain.order.entity.Order;
import com.spartaifive.commercepayment.domain.order.entity.OrderProduct;
import com.spartaifive.commercepayment.domain.order.repository.OrderProductRepository;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.order.util.OrderSupport;
import com.spartaifive.commercepayment.domain.product.entity.Product;
import com.spartaifive.commercepayment.domain.product.entity.ProductStatus;
import com.spartaifive.commercepayment.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    // TODO: 현재 선택한 상품에 대해서 조회 할 때 
    // 예를들어 고객이 포카칩, 사과 이렇게 샀을 때
    // 사과만 단종이 되었을 경우 바로 에러를 내뿜습니다.
    // 이게 좋은건지는 모르겠습니다.

    @Transactional
    public GetOrderResponse addOrder(AddOrderRequest req) {
        req = OrderSupport.NormalizeAddOrderRequest(req);

        Map<Long, AddOrderRequest.RequestProduct> productIdToReq = new HashMap<>();
        for (AddOrderRequest.RequestProduct reqP : req.getOrderProducts()) {
            Long id = reqP.getProductId();
            if (id != null) {
                productIdToReq.put(id, reqP);
            }
        }

        // 요청에서 들어온 상품 들을 조회
        // 이때 단종 이나 품정 상품은 제외
        List<Product> products = productRepository.findAllByStatusAndId(
                ProductStatus.ON_SALE, productIdToReq.keySet());

        // 이때 상품 목록이 없다면 에러를 반환
        // TODO: custom exception 생성
        if (products.size() <= 0) {
            throw new RuntimeException("선택된 상품이 없습니다.");
        }

        // 주문 상품의 갯수와 실제 상품의 갯수가 다를 경우 에러 반환
        if (products.size() != req.getOrderProducts().size()) {
            throw new RuntimeException("모든 주문을 완료 할 수 없습니다.");
        }

        // 주문 시도 양보다 재고가 적으면 에러 반환
        // TODO: 여기서 체크하는게 맞나요?
        for (final Product p : products) {
            Long quantity = productIdToReq.get(p.getId()).getQuantity();
            if (p.getStock() < quantity) {
                throw new RuntimeException(String.format(
                    "주문할려는 상품 %s의 재고(%s)보다 주문 갯수(%s)가 더 많습니다.",
                    p.getName(),
                    p.getStock(),
                    quantity
                ));
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

        // 주문 객체 생성
        // TODO: 현재 저희에게는 유저의 개념이 없기 때문에 일단은 0을 저장합니다.
        Order order = new Order(total, 0L);

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


    public GetOrderResponse getOrder(Long orderId) {
        // TODO: custom exception 생성
        Order order = orderRepository.findById(orderId).orElseThrow(()->
                new RuntimeException(String.format("이 %s id의 주문을 찾을 수 없습니다.", orderId)));

        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder_Id(order.getId());

        return GetOrderResponse.fromOrderAndOrderProducts(
                order,
                orderProducts);
    }

    public List<GetManyOrdersResponse> getManyOrders() {
        List<Order> orders = orderRepository.findAll();

        List<GetManyOrdersResponse> dtos = new ArrayList<>();

        for (final Order order : orders) {
            dtos.add(GetManyOrdersResponse.of(order));
        }

        return dtos;
    }
}
