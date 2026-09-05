package com.example.ecommerce.payment;

import java.math.BigDecimal;

public interface PaymentProvider {

    PaymentResult process(
            String orderNumber,
            BigDecimal amount
    );

    record PaymentResult(
            boolean successful,
            String transactionReference
    ) {
    }
}