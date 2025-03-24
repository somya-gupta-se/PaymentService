package com.training.PaymentService;

import com.training.PaymentService.client.OrderClient;
import com.training.PaymentService.model.Payment;
import com.training.PaymentService.model.PaymentStatus;
import com.training.PaymentService.repository.PaymentRepository;
import com.training.PaymentService.service.PaymentService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderClient orderClient;

    @InjectMocks
    private PaymentService  paymentService;

    private Payment mockPayment;

    private OrderClient.OrderResponse mockOrder;


    @BeforeEach
    void setup(){
        mockOrder = new OrderClient.OrderResponse(1L,1L,"ABC123", 2, BigDecimal.valueOf(50));
        mockPayment = new Payment();
        mockPayment.setAmount(BigDecimal.valueOf(50));
        mockPayment.setPaymentDate(LocalDateTime.now());
        mockPayment.setStatus(PaymentStatus.SUCCESS);
        mockPayment.setCustomerId(1L);
        mockPayment.setOrderId(1L);
        mockPayment.setId(1L);
    }

    @Test
    void testProcessPayment_Success(){
        //Arrange
        when(orderClient.getOrderById(1L)).thenReturn(mockOrder);
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);

        //aCT
        Payment result   = paymentService.processPayment(1L);

        //Assert
        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS,result.getStatus());
        assertEquals(BigDecimal.valueOf(50),result.getAmount());
        assertEquals(1L, result.getCustomerId());

        //vreify
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testProcessPayment_OrderNotFound(){
        when(orderClient.getOrderById(2L)).thenReturn(null);


    }
}
