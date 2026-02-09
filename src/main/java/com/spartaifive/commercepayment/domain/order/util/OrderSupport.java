package com.spartaifive.commercepayment.domain.order.util;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.spartaifive.commercepayment.domain.order.dto.AddOrderRequest;

public class OrderSupport {
    /**
     * AddOrderRequest는
     * <p>
     * 상품 ID - 상품 수량
     * <p>
     * 키 쌍으로 이루어져 있습니다. 이때 상품 ID 가 중복 될 경우, 예를 들어
     * <pre> {@code
     * 포카칩 - 2개
     * 포카칩 - 3개
     * }</pre>
     * 일 경우 하나로 합쳐
     * <p>
     * <pre>{@code 포카칩 - 5개로 만듭니다.} <pre/>
     * 으로 만듭니다.
     * @param req 정규화할 요청
     * @return 정규화된 요청
     */
    public static AddOrderRequest NormalizeAddOrderRequest(AddOrderRequest req) {
         Map<Long, Long> idToQuantity = new HashMap<>();

         for (final AddOrderRequest.RequestProduct p : req.getOrderProducts()) {
              if (idToQuantity.containsKey(p.getProductId())) {
                   Long quantity = p.getQuantity() + idToQuantity.get(p.getProductId());
                   idToQuantity.put(p.getProductId(), quantity);
              }else {
                   idToQuantity.put(p.getProductId(), p.getQuantity());
              }
         }

         List<AddOrderRequest.RequestProduct> reqProducts = new ArrayList<>();

         for (Map.Entry<Long, Long> entry : idToQuantity.entrySet()) {
              reqProducts.add(new AddOrderRequest.RequestProduct(
                    entry.getKey(),
                    entry.getValue()
               ));
         }

         return new AddOrderRequest(reqProducts);
    }
}
