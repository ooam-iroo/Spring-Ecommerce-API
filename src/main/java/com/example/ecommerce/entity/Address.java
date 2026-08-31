package com.example.ecommerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "addresses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "postal_code", nullable = false, length = 30)
    private String postalCode;

    @Column(nullable = false, length = 255)
    private String street;

    @Column(length = 50)
    private String building;

    @Column(length = 50)
    private String unit;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    public Address(
            User user,
            String title,
            String country,
            String city,
            String postalCode,
            String street,
            String building,
            String unit,
            boolean defaultAddress
    ) {
        this.user = user;
        this.title = title;
        this.country = country;
        this.city = city;
        this.postalCode = postalCode;
        this.street = street;
        this.building = building;
        this.unit = unit;
        this.defaultAddress = defaultAddress;
    }

    public void update(
            String title,
            String country,
            String city,
            String postalCode,
            String street,
            String building,
            String unit,
            boolean defaultAddress
    ) {
        this.title = title;
        this.country = country;
        this.city = city;
        this.postalCode = postalCode;
        this.street = street;
        this.building = building;
        this.unit = unit;
        this.defaultAddress = defaultAddress;
    }

    public void setDefaultAddress(boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }
}