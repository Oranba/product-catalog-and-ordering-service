package com.oranba.springboot.catalog.service.impl;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oranba.springboot.catalog.domain.model.Order;
import com.oranba.springboot.catalog.domain.model.OrderItem;
import com.oranba.springboot.catalog.domain.model.OrderStatus;
import com.oranba.springboot.catalog.repository.OrderItemRepository;
import com.oranba.springboot.catalog.repository.OrderRepository;
import com.oranba.springboot.catalog.service.OrderService;
import com.oranba.springboot.catalog.service.ProductService;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final MeterRegistry meterRegistry;

    // Timer metrics for performance monitoring
    private final Timer findOrderTimer;
    private final Timer createOrderTimer;
    private final Timer updateStatusTimer;

    @Autowired
    public OrderServiceImpl (OrderRepository orderRepository,
                             OrderItemRepository orderItemRepository,
                             ProductService productService,
                             MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
        this.meterRegistry = meterRegistry;

        // Initialize performance metrics
        this.findOrderTimer = Timer.builder("order.find.time").description("Time taken to find orders").register(meterRegistry);
        this.createOrderTimer = Timer.builder("order.create.time").description("Time taken to create orders").register(meterRegistry);
        this.updateStatusTimer = Timer.builder("order.status.update.time")
                                      .description("Time taken to update order status")
                                      .register(meterRegistry);
    }

    @Override
    public Page<Order> findAllOrders (Pageable pageable, Map<String, String> filters) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Finding all orders with filters: {}", filters);

            if (filters == null) {
                filters = new HashMap<>();
            }

            // Extract filter parameters
            String customerId = filters.getOrDefault("customerId", null);
            String status = filters.getOrDefault("status", null);
            String startDate = filters.getOrDefault("startDate", null);
            String endDate = filters.getOrDefault("endDate", null);

            // Apply filters based on provided parameters
            if (customerId != null && status != null) {
                return orderRepository.findByCustomerIdAndOrderStatus(Long.valueOf(customerId), OrderStatus.valueOf(status), pageable);
            }
            else if (customerId != null) {
                return orderRepository.findByCustomerId(Long.valueOf(customerId), pageable);
            }
            else if (status != null) {
                return orderRepository.findByOrderStatus(OrderStatus.valueOf(status), pageable);
            }
            else if (startDate != null && endDate != null) {
                LocalDateTime start = LocalDateTime.parse(startDate);
                LocalDateTime end = LocalDateTime.parse(endDate);
                return orderRepository.findByCreatedAtBetween(start, end, pageable);
            }
            else {
                return orderRepository.findAll(pageable);
            }
        }
        finally {
            sample.stop(findOrderTimer);
        }
    }

    @Override
    public Optional<Order> findOrderById (Long id) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Finding order with ID: {}", id);
            return orderRepository.findById(id);
        }
        finally {
            sample.stop(findOrderTimer);
        }
    }

    @Override
    @Transactional
    public Order createOrder (Order order) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.info("Creating new order for customer: {}", order.getCustomerId());

            // Generate unique order number
            order.setOrderNumber("ORD-" + System.currentTimeMillis());
            order.setOrderStatus(OrderStatus.CREATED);

            // Save the order first to get the ID
            Order savedOrder = orderRepository.save(order);

            // Save order items and update inventory
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    // Set the order ID for the item
                    item.setOrderId(savedOrder.getId());
                    orderItemRepository.save(item);

                    // Update product inventory
                    productService.updateInventory(item.getProductId(), -item.getQuantity());
                }
            }

            // Publish order created event (to be implemented)
            // orderEventProducer.publishOrderCreatedEvent(savedOrder);

            return savedOrder;
        }
        finally {
            sample.stop(createOrderTimer);
        }
    }

    @Override
    @Transactional
    public Order updateOrderStatus (Long id, OrderStatus status) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.info("Updating order status to {} for order ID: {}", status, id);

            Optional<Order> existingOrder = orderRepository.findById(id);

            if (existingOrder.isPresent()) {
                Order orderToUpdate = existingOrder.get();
                OrderStatus previousStatus = orderToUpdate.getOrderStatus();

                // Validate status transition
                validateStatusTransition(previousStatus, status);

                orderToUpdate.setOrderStatus(status);
                Order updatedOrder = orderRepository.save(orderToUpdate);

                // Publish order status changed event (to be implemented)
                // orderEventProducer.publishOrderStatusChangedEvent(updatedOrder, previousStatus);

                return updatedOrder;
            }
            else {
                throw new RuntimeException("Order not found with ID: " + id);
            }
        }
        finally {
            sample.stop(updateStatusTimer);
        }
    }

    private void validateStatusTransition (OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid transitions
        Map<OrderStatus, Set<OrderStatus>> validTransitions = new HashMap<>();
        validTransitions.put(OrderStatus.CREATED, new HashSet<>(Arrays.asList(OrderStatus.PAID, OrderStatus.CANCELLED)));
        validTransitions.put(OrderStatus.PAID, new HashSet<>(Arrays.asList(OrderStatus.SHIPPED, OrderStatus.CANCELLED)));
        validTransitions.put(OrderStatus.SHIPPED, new HashSet<>(Arrays.asList(OrderStatus.DELIVERED, OrderStatus.CANCELLED)));
        validTransitions.put(OrderStatus.DELIVERED, new HashSet<>(Collections.emptyList()));
        validTransitions.put(OrderStatus.CANCELLED, new HashSet<>(Collections.emptyList()));

        // Check if transition is valid
        if (!validTransitions.containsKey(currentStatus) || !validTransitions.get(currentStatus).contains(newStatus)) {
            throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    @Override
    public Page<Order> findOrdersByCustomer (Long customerId, Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Finding orders for customer ID: {}", customerId);
            return orderRepository.findByCustomerId(customerId, pageable);
        }
        finally {
            sample.stop(findOrderTimer);
        }
    }

    @Override
    public Page<Order> findOrdersInDateRange (LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Finding orders between dates: {} and {}", startDate, endDate);
            return orderRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        }
        finally {
            sample.stop(findOrderTimer);
        }
    }

    @Override
    public Page<Order> findOrdersByStatus (OrderStatus status, Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Finding orders with status: {}", status);
            return orderRepository.findByOrderStatus(status, pageable);
        }
        finally {
            sample.stop(findOrderTimer);
        }
    }

    @Override
    public Map<String, Object> getOrderMetrics () {
        logger.debug("Calculating order metrics");

        Map<String, Object> metrics = new HashMap<>();

        // Count orders by status
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByOrderStatus(status);
            metrics.put(status.toString(), count);
        }

        // Add total order count
        long totalOrders = orderRepository.count();
        metrics.put("TOTAL", totalOrders);

        return metrics;
    }
}
