package com.example.ecommerce.entity;

import com.example.ecommerce.entity.status.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            unique = true
    )
    private Order order;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    public Payment(
            Order order,
            BigDecimal amount,
            PaymentStatus status,
            String transactionReference
    ) {
        this.order = order;
        this.amount = amount;
        this.status = status;
        this.transactionReference = transactionReference;
    }

    public void markSuccessful(String transactionReference) {
        this.status = PaymentStatus.SUCCESS;
        this.transactionReference = transactionReference;
    }

    public void markFailed(String transactionReference) {
        this.status = PaymentStatus.FAILED;
        this.transactionReference = transactionReference;
    }

    public void refund() {
        if (status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Only successful payments can be refunded"
            );
        }

        this.status = PaymentStatus.REFUNDED;
    }
}