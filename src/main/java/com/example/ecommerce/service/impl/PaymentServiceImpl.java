package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.payment.PaymentResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.entity.status.OrderStatus;
import com.example.ecommerce.entity.status.PaymentStatus;
import com.example.ecommerce.exception.BusinessException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.payment.PaymentProvider;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentProvider paymentProvider;

    @Override
    public PaymentResponse processPayment(Long orderId) {

        User currentUser = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + orderId
                        )
                );

        validateOrderAccess(order, currentUser);
        validateOrderCanBePaid(order);

        if (paymentRepository.existsByOrderId(orderId)) {
            throw new BusinessException(
                    "Payment already exists for order: " + orderId
            );
        }

        PaymentProvider.PaymentResult result =
                paymentProvider.process(
                        order.getOrderNumber(),
                        order.getTotal()
                );

        Payment payment = new Payment(
                order,
                order.getTotal(),
                result.successful()
                        ? PaymentStatus.SUCCESS
                        : PaymentStatus.FAILED,
                result.transactionReference()
        );

        Payment savedPayment = paymentRepository.save(payment);

        return toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse findByOrderId(Long orderId) {

        User currentUser = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + orderId
                        )
                );

        validateOrderAccess(order, currentUser);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found for order: " + orderId
                        )
                );

        return toResponse(payment);
    }

    @Override
    public PaymentResponse refundPayment(Long orderId) {

        User currentUser = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + orderId
                        )
                );

        validateOrderAccess(order, currentUser);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found for order: " + orderId
                        )
                );

        try {
            payment.refund();
        } catch (IllegalStateException exception) {
            throw new BusinessException(exception.getMessage());
        }

        return toResponse(payment);
    }

    private void validateOrderCanBePaid(Order order) {

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(
                    "Cancelled orders cannot be paid"
            );
        }
    }

    private void validateOrderAccess(
            Order order,
            User currentUser
    ) {
        boolean isOwner =
                order.getUser().getId().equals(currentUser.getId());

        if (!isOwner) {
            throw new BusinessException(
                    "You do not have access to this order"
            );
        }
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new BusinessException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + email
                        )
                );
    }

    private PaymentResponse toResponse(Payment payment) {

        Order order = payment.getOrder();

        return new PaymentResponse(
                payment.getId(),
                order.getId(),
                order.getOrderNumber(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionReference(),
                payment.getCreatedAt()
        );
    }
}