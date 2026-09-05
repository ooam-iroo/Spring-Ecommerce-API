package com.example.ecommerce.service;

import com.example.ecommerce.dto.payment.PaymentResponse;

public interface PaymentService {

    PaymentResponse processPayment(Long orderId);

    PaymentResponse findByOrderId(Long orderId);

    PaymentResponse refundPayment(Long orderId);
}