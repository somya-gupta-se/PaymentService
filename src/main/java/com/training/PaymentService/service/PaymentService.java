package com.training.PaymentService.service;
import com.training.PaymentService.client.OrderClient;
import com.training.PaymentService.event.PaymentSuccessEvent;
import com.training.PaymentService.exception.PaymentFailedException;
import com.training.PaymentService.model.Payment;
import com.training.PaymentService.model.PaymentStatus;
import com.training.PaymentService.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Autowired
    private  PaymentRepository paymentRepository;
    @Autowired
    private  OrderClient orderClient;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    private static final String PAYMENT_RETRY_INSTANCE = "PaymentService";


    @Retry(name = PAYMENT_RETRY_INSTANCE, fallbackMethod = "fallbackProcessPayment")
    public Payment processPayment(Long orderId) {
        boolean paymentSuccess = false;
        LOGGER.info("Communicating with order service to check if order is present");
        OrderClient.OrderResponse order = orderClient.getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found for ID: " + orderId);
        }
        Payment payment = new Payment();
        payment.setOrderId(order.id());
        payment.setCustomerId(order.customerId());
        payment.setAmount(order.totalPrice());
        payment.setStatus(PaymentStatus.SUCCESS); // Simulating successful payment
        payment.setPaymentDate(LocalDateTime.now());
        LOGGER.info("Payment successful for Order ID: {} triggering payment success event", orderId);
        paymentSuccess = true;
        String value = "for customer id "+order.customerId()+" and order id is "+orderId;
        kafkaTemplate.send("paymentTopic", "PAYMENT_SUCCESS", value);

        LOGGER.info("updating order id {} status from PLACED to CONFIRMED", orderId);

        orderClient.updateOrder(orderId);
        if (!paymentSuccess) {
            throw new PaymentFailedException("Payment processing failed for order ID " + orderId);
        }

        return paymentRepository.save(payment);
    }

    public Payment fallbackProcessPayment(Long orderId, Throwable throwable) {
        LOGGER.error("Payment processing failed for Order ID: {}. Reason: {}", orderId, throwable.getMessage());
        Payment failedPayment = new Payment();
        failedPayment.setOrderId(orderId);
        failedPayment.setStatus(PaymentStatus.FAILED);
        failedPayment.setPaymentDate(LocalDateTime.now());

        return failedPayment;
    }

    public Optional<Payment> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}