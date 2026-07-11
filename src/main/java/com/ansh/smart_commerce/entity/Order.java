package com.ansh.smart_commerce.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ansh.smart_commerce.enums.OrderStatus;

import jakarta.persistence.*;

/**
 * Represents a placed order in Smart Commerce.
 *
 * Key design notes:
 * - @Table(name = "orders") because "order" is a reserved SQL keyword — always rename!
 * - totalAmount is stored at order-time (snapshot), not recalculated from products,
 *   because product prices can change after the order is placed.
 * - @Enumerated(EnumType.STRING) stores "PENDING" in DB, not 0/1/2 (ordinal).
 *   This is CRITICAL — if you insert a new enum value in the middle, ordinals shift
 *   and all existing DB rows become corrupted. Always use STRING.
 * - CascadeType.ALL on orderItems means saving/deleting an Order
 *   automatically saves/deletes its OrderItems (parent controls child lifecycle).
 * - orphanRemoval = true means if an OrderItem is removed from the list,
 *   it's deleted from DB automatically without a separate delete call.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime orderDate;


    private double totalAmount;
    
    @Column(nullable = false)
    private double subtotal;

    @Column(nullable = false)
    private double discountAmount = 0;

    private String couponCode;

    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    
    public Order() {
    }

    public Order(User user, LocalDateTime orderDate, double totalAmount, OrderStatus status, String couponCode, double subtotal, double discountAmount) {
        this.user = user;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.couponCode = couponCode;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
    }

  

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    public double getSubtotal() {
        return subtotal;
    }
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
    public double getDiscountAmount() {
        return discountAmount;
    }
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }
    public String getCouponCode() {
        return couponCode;
    }
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getShippingName() {
        return shippingName;
    }
    public void setShippingName(String shippingName) {
        this.shippingName = shippingName;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }
    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
