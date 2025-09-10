package com.oranba.springboot.catalog.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oranba.springboot.catalog.config.CacheConfig;
import com.oranba.springboot.catalog.domain.model.Product;
import com.oranba.springboot.catalog.repository.ProductRepository;
import com.oranba.springboot.catalog.service.ProductService;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final MeterRegistry meterRegistry;

    // Timer metrics for performance monitoring
    private final Timer findProductTimer;
    private final Timer updateInventoryTimer;

    @Autowired
    public ProductServiceImpl (ProductRepository productRepository, MeterRegistry meterRegistry) {
        this.productRepository = productRepository;
        this.meterRegistry = meterRegistry;

        // Initialize performance metrics
        this.findProductTimer = Timer.builder("product.find.time").description("Time taken to find products").register(meterRegistry);
        this.updateInventoryTimer = Timer.builder("product.inventory.update.time")
                                         .description("Time taken to update product inventory")
                                         .register(meterRegistry);
    }

    @Override
    @Cacheable(value = CacheConfig.PRODUCTS_CACHE, key = "'all:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #filters")
    public Page<Product> findAllProducts (Pageable pageable, Map<String, String> filters) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            if (filters == null) {
                filters = new HashMap<>();
            }

            // Extract filter parameters
            String name = filters.getOrDefault("name", null);
            String categoryId = filters.getOrDefault("category", null);
            String minPrice = filters.getOrDefault("minPrice", null);
            String maxPrice = filters.getOrDefault("maxPrice", null);

            // Apply filters based on provided parameters
            if (categoryId != null && name != null) {
                return productRepository.findByNameContainingIgnoreCaseAndCategoryId(name, Long.valueOf(categoryId), pageable);
            }
            else if (categoryId != null) {
                return productRepository.findByCategoryId(Long.valueOf(categoryId), pageable);
            }
            else if (name != null) {
                return productRepository.findByNameContainingIgnoreCase(name, pageable);
            }
            else if (minPrice != null && maxPrice != null) {
                return productRepository.findByPriceBetween(new BigDecimal(minPrice), new BigDecimal(maxPrice), pageable);
            }
            else {
                return productRepository.findAll(pageable);
            }
        }
        finally {
            sample.stop(findProductTimer);
        }
    }

    @Override
    @Cacheable(value = CacheConfig.PRODUCT_DETAILS_CACHE, key = "#id")
    public Optional<Product> findProductById (Long id) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Finding product with ID: {}", id);
            return productRepository.findById(id);
        }
        finally {
            sample.stop(findProductTimer);
        }
    }

    @Override
    @Transactional
    public Product createProduct (Product product) {
        logger.info("Creating new product: {}", product.getName());
        return productRepository.save(product);
    }

    @Override
    @Transactional
    @CachePut(value = CacheConfig.PRODUCT_DETAILS_CACHE, key = "#id")
    @CacheEvict(value = CacheConfig.PRODUCTS_CACHE, allEntries = true)
    public Product updateProduct (Long id, Product product) {
        logger.info("Updating product with ID: {}", id);

        Optional<Product> existingProduct = productRepository.findById(id);

        if (existingProduct.isPresent()) {
            Product productToUpdate = existingProduct.get();

            // Update fields
            productToUpdate.setName(product.getName());
            productToUpdate.setDescription(product.getDescription());
            productToUpdate.setPrice(product.getPrice());
            productToUpdate.setCategoryId(product.getCategoryId());
            productToUpdate.setImageUrl(product.getImageUrl());
            productToUpdate.setIsActive(product.getIsActive());

            // Don't update inventory through this method

            return productRepository.save(productToUpdate);
        }
        else {
            throw new RuntimeException("Product not found with ID: " + id);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { CacheConfig.PRODUCTS_CACHE, CacheConfig.PRODUCT_DETAILS_CACHE }, key = "#id")
    public void deleteProduct (Long id) {
        logger.info("Deleting product with ID: {}", id);

        Optional<Product> product = productRepository.findById(id);

        if (product.isPresent()) {
            Product productToDelete = product.get();
            productToDelete.setIsActive(false);
            productRepository.save(productToDelete);
        }
        else {
            throw new RuntimeException("Product not found with ID: " + id);
        }
    }

    @Override
    @Cacheable(value = CacheConfig.PRODUCTS_CACHE, key = "'category:' + #categoryId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<Product> findProductsByCategory (Long categoryId, Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Finding products by category ID: {}", categoryId);
            return productRepository.findByCategoryId(categoryId, pageable);
        }
        finally {
            sample.stop(findProductTimer);
        }
    }

    @Override
    @Transactional
    @CachePut(value = CacheConfig.PRODUCT_DETAILS_CACHE, key = "#productId")
    @CacheEvict(value = CacheConfig.PRODUCTS_CACHE, allEntries = true)
    public Product updateInventory (Long productId, int quantityChange) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.info("Updating inventory for product ID: {} by {}", productId, quantityChange);

            Optional<Product> product = productRepository.findById(productId);

            if (product.isPresent()) {
                Product productToUpdate = product.get();
                int newInventory = productToUpdate.getInventory() + quantityChange;

                if (newInventory < 0) {
                    throw new RuntimeException("Insufficient inventory for product: " + productId);
                }

                productToUpdate.setInventory(newInventory);
                return productRepository.save(productToUpdate);
            }
            else {
                throw new RuntimeException("Product not found with ID: " + productId);
            }
        }
        finally {
            sample.stop(updateInventoryTimer);
        }
    }

    @Override
    public Iterable<Product> findProductsWithLowInventory (Integer threshold) {
        logger.debug("Finding products with inventory below threshold: {}", threshold);
        return productRepository.findProductsWithLowInventory(threshold);
    }
}
