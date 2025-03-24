package com.training.PaymentService.controller;
import com.training.PaymentService.model.Payment;
import com.training.PaymentService.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    //@PostMapping("/user/process/{orderId}")
    @PostMapping("/process/{orderId}")
    public ResponseEntity<Payment> processPayment(@PathVariable Long orderId) {
        Payment payment = paymentService.processPayment(orderId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable Long orderId) {
        Optional<Payment> payment = paymentService.getPaymentByOrderId(orderId);
        return payment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
