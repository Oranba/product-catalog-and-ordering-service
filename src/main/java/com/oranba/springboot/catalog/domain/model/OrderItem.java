package com.oranba.springboot.catalog.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_at_order", nullable = false)
    private BigDecimal priceAtOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate () {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate () {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }

    public Long getOrderId () {
        return orderId;
    }

    public void setOrderId (Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId () {
        return productId;
    }

    public void setProductId (Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity () {
        return quantity;
    }

    public void setQuantity (Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtOrder () {
        return priceAtOrder;
    }

    public void setPriceAtOrder (BigDecimal priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

    public LocalDateTime getCreatedAt () {
        return createdAt;
    }

    public void setCreatedAt (LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt () {
        return updatedAt;
    }

    public void setUpdatedAt (LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
