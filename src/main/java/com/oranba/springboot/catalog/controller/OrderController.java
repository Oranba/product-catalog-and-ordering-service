package com.oranba.springboot.catalog.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.oranba.springboot.catalog.domain.model.Order;
import com.oranba.springboot.catalog.domain.model.OrderStatus;
import com.oranba.springboot.catalog.service.OrderService;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    @Autowired
    public OrderController (OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Timed(value = "api.order.findAll", description = "Time taken to find all orders")
    public ResponseEntity<Page<Order>> getAllOrders (@PageableDefault(size = 20) Pageable pageable,
                                                     @RequestParam(required = false) Map<String, String> filters) {

        logger.debug("REST request to get all Orders with filters: {}", filters);
        Page<Order> page = orderService.findAllOrders(pageable, filters);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Timed(value = "api.order.findById", description = "Time taken to find order by ID")
    public ResponseEntity<Order> getOrderById (@PathVariable Long id) {
        logger.debug("REST request to get Order : {}", id);

        Optional<Order> order = orderService.findOrderById(id);
        return order.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Timed(value = "api.order.create", description = "Time taken to create an order")
    public ResponseEntity<Order> createOrder (@RequestBody Order order) {
        logger.debug("REST request to save Order for customer: {}", order.getCustomerId());

        if (order.getId() != null) {
            return ResponseEntity.badRequest().header("error", "A new order cannot have an ID").build();
        }

        Order result = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}/status")
    @Timed(value = "api.order.updateStatus", description = "Time taken to update order status")
    public ResponseEntity<Order> updateOrderStatus (@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {

        logger.debug("REST request to update Order status : {}, {}", id, statusUpdate);

        String statusStr = statusUpdate.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().header("error", "Status is required").build();
        }

        try {
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            Order result = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(result);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().header("error", "Invalid status: " + statusStr).build();
        }
        catch (IllegalStateException e) {
            return ResponseEntity.badRequest().header("error", e.getMessage()).build();
        }
    }

    @GetMapping("/customer/{customerId}")
    @Timed(value = "api.order.findByCustomer", description = "Time taken to find orders by customer")
    public ResponseEntity<Page<Order>> getOrdersByCustomer (@PathVariable Long customerId, @PageableDefault(size = 20) Pageable pageable) {

        logger.debug("REST request to get Orders by customer : {}", customerId);
        Page<Order> page = orderService.findOrdersByCustomer(customerId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/status/{status}")
    @Timed(value = "api.order.findByStatus", description = "Time taken to find orders by status")
    public ResponseEntity<Page<Order>> getOrdersByStatus (@PathVariable String status, @PageableDefault(size = 20) Pageable pageable) {

        logger.debug("REST request to get Orders by status : {}", status);
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            Page<Order> page = orderService.findOrdersByStatus(orderStatus, pageable);
            return ResponseEntity.ok(page);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/date-range")
    @Timed(value = "api.order.findByDateRange", description = "Time taken to find orders by date range")
    public ResponseEntity<Page<Order>> getOrdersInDateRange (@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                                             @PageableDefault(size = 20) Pageable pageable) {

        logger.debug("REST request to get Orders between dates : {} and {}", startDate, endDate);
        Page<Order> page = orderService.findOrdersInDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/metrics")
    @Timed(value = "api.order.metrics", description = "Time taken to get order metrics")
    public ResponseEntity<Map<String, Object>> getOrderMetrics () {
        logger.debug("REST request to get Order metrics");

        Map<String, Object> metrics = orderService.getOrderMetrics();
        return ResponseEntity.ok(metrics);
    }
}
