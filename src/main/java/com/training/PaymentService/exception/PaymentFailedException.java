package com.training.PaymentService.exception;

public class PaymentFailedException extends RuntimeException{
    public PaymentFailedException(String message) {
        super(message);
    }
}
