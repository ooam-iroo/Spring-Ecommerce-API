package com.example.ecommerce.entity;

import com.example.ecommerce.exception.BusinessException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "carts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @OneToMany(
            mappedBy = "cart",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItem> items = new ArrayList<>();

    public Cart(User user) {
        this.user = user;
    }

    public void addItem(Product product, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Cart item quantity must be greater than zero"
            );
        }

        CartItem existingItem = items.stream()
                .filter(item ->
                        item.getProduct().getId().equals(product.getId())
                )
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.increaseQuantity(quantity);
            return;
        }

        CartItem cartItem = new CartItem(
                this,
                product,
                quantity
        );

        items.add(cartItem);
    }

    public void updateItemQuantity(
            Long productId,
            int quantity
    ) {
        CartItem item = findItem(productId);

        item.updateQuantity(quantity);
    }

    public void removeItem(Long productId) {
        CartItem item = findItem(productId);

        items.remove(item);
    }

    public void clear() {
        items.clear();
    }

    private CartItem findItem(Long productId) {
        return items.stream()
                .filter(item ->
                        item.getProduct().getId().equals(productId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException(
                                "Product is not in cart: " + productId
                        )
                );
    }
}