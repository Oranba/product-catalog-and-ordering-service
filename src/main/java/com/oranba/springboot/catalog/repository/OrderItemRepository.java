package com.oranba.springboot.catalog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oranba.springboot.catalog.domain.model.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Find order items by order ID
    List<OrderItem> findByOrderId (Long orderId);

    // Find order items by product ID
    List<OrderItem> findByProductId (Long productId);

    // Count number of times a product has been ordered
    long countByProductId (Long productId);

    // Delete all items for a specific order
    void deleteByOrderId (Long orderId);
}
