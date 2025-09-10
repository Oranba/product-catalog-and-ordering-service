package com.oranba.springboot.catalog.controller;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.oranba.springboot.catalog.domain.model.Product;
import com.oranba.springboot.catalog.service.ProductService;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    @Autowired
    public ProductController (ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Timed(value = "api.product.findAll", description = "Time taken to find all products")
    public ResponseEntity<Page<Product>> getAllProducts (@PageableDefault(size = 20) Pageable pageable,
                                                         @RequestParam(required = false) Map<String, String> filters) {

        logger.debug("REST request to get all Products with filters: {}", filters);
        Page<Product> page = productService.findAllProducts(pageable, filters);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Timed(value = "api.product.findById", description = "Time taken to find product by ID")
    public ResponseEntity<Product> getProductById (@PathVariable Long id) {
        logger.debug("REST request to get Product : {}", id);

        Optional<Product> product = productService.findProductById(id);
        return product.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Timed(value = "api.product.create", description = "Time taken to create a product")
    public ResponseEntity<Product> createProduct (@RequestBody Product product) {
        logger.debug("REST request to save Product : {}", product);

        if (product.getId() != null) {
            return ResponseEntity.badRequest().header("error", "A new product cannot have an ID").build();
        }

        Product result = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    @Timed(value = "api.product.update", description = "Time taken to update a product")
    public ResponseEntity<Product> updateProduct (@PathVariable Long id, @RequestBody Product product) {

        logger.debug("REST request to update Product : {}, {}", id, product);

        Product result = productService.updateProduct(id, product);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @Timed(value = "api.product.delete", description = "Time taken to delete a product")
    public ResponseEntity<Void> deleteProduct (@PathVariable Long id) {
        logger.debug("REST request to delete Product : {}", id);

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{categoryId}")
    @Timed(value = "api.product.findByCategory", description = "Time taken to find products by category")
    public ResponseEntity<Page<Product>> getProductsByCategory (@PathVariable Long categoryId,
                                                                @PageableDefault(size = 20) Pageable pageable) {

        logger.debug("REST request to get Products by category : {}", categoryId);
        Page<Product> page = productService.findProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}/inventory")
    @Timed(value = "api.product.updateInventory", description = "Time taken to update product inventory")
    public ResponseEntity<Product> updateInventory (@PathVariable Long id, @RequestParam int quantityChange) {

        logger.debug("REST request to update inventory for Product : {}, change: {}", id, quantityChange);

        Product result = productService.updateInventory(id, quantityChange);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/low-inventory")
    @Timed(value = "api.product.findLowInventory", description = "Time taken to find low inventory products")
    public ResponseEntity<Iterable<Product>> getProductsWithLowInventory (@RequestParam(defaultValue = "10") Integer threshold) {

        logger.debug("REST request to get Products with inventory below : {}", threshold);
        Iterable<Product> products = productService.findProductsWithLowInventory(threshold);
        return ResponseEntity.ok(products);
    }
}
