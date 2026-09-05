package com.example.ecommerce.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public PaymentResult process(
            String orderNumber,
            BigDecimal amount
    ) {
        String transactionReference =
                "TXN-" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();

        return new PaymentResult(
                true,
                transactionReference
        );
    }
}