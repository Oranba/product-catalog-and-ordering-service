package com.oranba.springboot.catalog.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.oranba.springboot.catalog.domain.model.Product;

public interface ProductService {

    /**
     * Find all products with pagination and filtering
     * 
     * @param pageable
     *        Pagination information
     * @param filters
     *        Map of filter criteria
     * @return Page of products
     */
    Page<Product> findAllProducts (Pageable pageable, Map<String, String> filters);

    /**
     * Find a product by its ID
     * 
     * @param id
     *        Product ID
     * @return Optional containing the product if found
     */
    Optional<Product> findProductById (Long id);

    /**
     * Create a new product
     * 
     * @param product
     *        Product to create
     * @return Created product
     */
    Product createProduct (Product product);

    /**
     * Update an existing product
     * 
     * @param id
     *        Product ID
     * @param product
     *        Updated product data
     * @return Updated product
     */
    Product updateProduct (Long id, Product product);

    /**
     * Delete a product (logical delete)
     * 
     * @param id
     *        Product ID
     */
    void deleteProduct (Long id);

    /**
     * Find products by category
     * 
     * @param categoryId
     *        Category ID
     * @param pageable
     *        Pagination information
     * @return Page of products
     */
    Page<Product> findProductsByCategory (Long categoryId, Pageable pageable);

    /**
     * Update product inventory
     * 
     * @param productId
     *        Product ID
     * @param quantityChange
     *        Quantity change (positive for increase, negative for decrease)
     * @return Updated product
     */
    Product updateInventory (Long productId, int quantityChange);

    /**
     * Find products with low inventory
     * 
     * @param threshold
     *        Low inventory threshold
     * @return List of products with inventory below threshold
     */
    Iterable<Product> findProductsWithLowInventory (Integer threshold);
}
