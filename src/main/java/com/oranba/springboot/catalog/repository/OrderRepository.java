package com.oranba.springboot.catalog.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oranba.springboot.catalog.domain.model.Order;
import com.oranba.springboot.catalog.domain.model.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders by customer ID
    Page<Order> findByCustomerId (Long customerId, Pageable pageable);

    // Find orders by status
    Page<Order> findByOrderStatus (OrderStatus status, Pageable pageable);

    // Find orders by customer ID and status
    Page<Order> findByCustomerIdAndOrderStatus (Long customerId, OrderStatus status, Pageable pageable);

    // Find orders created within a date range
    Page<Order> findByCreatedAtBetween (LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find order by order number
    Order findByOrderNumber (String orderNumber);

    // Count orders by status
    long countByOrderStatus (OrderStatus status);

    // Find recent orders
    List<Order> findTop10ByOrderByCreatedAtDesc ();
}
