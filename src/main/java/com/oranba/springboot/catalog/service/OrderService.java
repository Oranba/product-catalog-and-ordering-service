package com.oranba.springboot.catalog.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.oranba.springboot.catalog.domain.model.Order;
import com.oranba.springboot.catalog.domain.model.OrderStatus;

public interface OrderService {

    /**
     * Find all orders with pagination and filtering
     * 
     * @param pageable
     *        Pagination information
     * @param filters
     *        Map of filter criteria
     * @return Page of orders
     */
    Page<Order> findAllOrders (Pageable pageable, Map<String, String> filters);

    /**
     * Find an order by its ID
     * 
     * @param id
     *        Order ID
     * @return Optional containing the order if found
     */
    Optional<Order> findOrderById (Long id);

    /**
     * Create a new order
     * 
     * @param order
     *        Order to create
     * @return Created order
     */
    Order createOrder (Order order);

    /**
     * Update order status
     * 
     * @param id
     *        Order ID
     * @param status
     *        New order status
     * @return Updated order
     */
    Order updateOrderStatus (Long id, OrderStatus status);

    /**
     * Find orders by customer ID with pagination
     * 
     * @param customerId
     *        Customer ID
     * @param pageable
     *        Pagination information
     * @return Page of customer orders
     */
    Page<Order> findOrdersByCustomer (Long customerId, Pageable pageable);

    /**
     * Find orders created between two dates
     * 
     * @param startDate
     *        Start date
     * @param endDate
     *        End date
     * @param pageable
     *        Pagination information
     * @return Page of orders within date range
     */
    Page<Order> findOrdersInDateRange (LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find orders by status with pagination
     * 
     * @param status
     *        Order status
     * @param pageable
     *        Pagination information
     * @return Page of orders with the specified status
     */
    Page<Order> findOrdersByStatus (OrderStatus status, Pageable pageable);

    /**
     * Get order metrics
     * 
     * @return Map with order metrics (count by status, etc.)
     */
    Map<String, Object> getOrderMetrics ();
}
