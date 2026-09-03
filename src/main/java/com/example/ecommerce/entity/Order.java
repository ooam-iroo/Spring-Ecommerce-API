package com.example.ecommerce.entity;

import com.example.ecommerce.entity.status.OrderStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discount;

    @Column(name = "shipping_cost", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingCost;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    public Order(
            String orderNumber,
            User user,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal shippingCost
    ) {
        this.orderNumber = orderNumber;
        this.user = user;
        this.status = OrderStatus.PENDING;
        this.subtotal = subtotal;
        this.discount = discount;
        this.shippingCost = shippingCost;
        this.total = subtotal
                .subtract(discount)
                .add(shippingCost);
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public void cancel() {
        if (status != OrderStatus.PENDING
                && status != OrderStatus.CONFIRMED) {

            throw new IllegalStateException(
                    "Order cannot be cancelled in status: " + status
            );
        }

        this.status = OrderStatus.CANCELLED;
    }
}