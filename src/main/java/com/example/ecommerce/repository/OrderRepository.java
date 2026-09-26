package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.status.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("""
        select coalesce(sum(o.total), 0)
        from Order o
        where o.status <> com.example.ecommerce.entity.status.OrderStatus.CANCELLED
        """)
    BigDecimal calculateTotalRevenue();

    long countByStatus(OrderStatus status);
}