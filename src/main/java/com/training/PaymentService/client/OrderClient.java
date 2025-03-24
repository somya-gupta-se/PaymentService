package com.training.PaymentService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "OrderService", path = "/order")
public interface OrderClient {

    @GetMapping("/{orderId}")
    OrderResponse getOrderById(@PathVariable Long orderId);

    record OrderResponse(Long id, Long customerId, String productId, int quantity, BigDecimal totalPrice) {}

    @GetMapping("/process/{orderId}")
    public void updateOrder(@PathVariable Long orderId);
}