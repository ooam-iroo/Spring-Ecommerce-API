package com.example.ecommerce.entity;

import com.example.ecommerce.entity.status.CouponType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "coupons")
public class Coupon extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    @Column(name = "minimum_order_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumOrderAmount;

    @Column(name = "maximum_discount", precision = 19, scale = 2)
    private BigDecimal maximumDiscount;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "expiration_date", nullable = false)
    private OffsetDateTime expirationDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "coupon")
    private List<Order> orders = new ArrayList<>();
}