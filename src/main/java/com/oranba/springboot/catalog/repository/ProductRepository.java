package com.oranba.springboot.catalog.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.oranba.springboot.catalog.domain.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all active products
    List<Product> findByIsActiveTrue ();

    // Find products by category
    Page<Product> findByCategoryId (Long categoryId, Pageable pageable);

    // Find products by name containing the search term
    Page<Product> findByNameContainingIgnoreCase (String name, Pageable pageable);

    // Find products by price range
    Page<Product> findByPriceBetween (BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Find products by category and price range
    Page<Product> findByCategoryIdAndPriceBetween (Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Find products by name and category
    Page<Product> findByNameContainingIgnoreCaseAndCategoryId (String name, Long categoryId, Pageable pageable);

    // Find products with low inventory (less than specified threshold)
    @Query("SELECT p FROM Product p WHERE p.inventory <= :threshold AND p.isActive = true")
    List<Product> findProductsWithLowInventory (@Param("threshold") Integer threshold);

    // Find product by SKU
    Product findBySku (String sku);
}
