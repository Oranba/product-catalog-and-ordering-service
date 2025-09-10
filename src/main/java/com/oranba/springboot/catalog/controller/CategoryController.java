package com.oranba.springboot.catalog.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.oranba.springboot.catalog.domain.model.Category;
import com.oranba.springboot.catalog.service.CategoryService;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    @Autowired
    public CategoryController (CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Timed(value = "api.category.findAll", description = "Time taken to find all categories")
    public ResponseEntity<List<Category>> getAllCategories () {
        logger.debug("REST request to get all Categories");
        List<Category> categories = categoryService.findAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Timed(value = "api.category.findById", description = "Time taken to find category by ID")
    public ResponseEntity<Category> getCategoryById (@PathVariable Long id) {
        logger.debug("REST request to get Category : {}", id);

        Optional<Category> category = categoryService.findCategoryById(id);
        return category.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Timed(value = "api.category.create", description = "Time taken to create a category")
    public ResponseEntity<Category> createCategory (@RequestBody Category category) {
        logger.debug("REST request to save Category : {}", category);

        if (category.getId() != null) {
            return ResponseEntity.badRequest().header("error", "A new category cannot have an ID").build();
        }

        Category result = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    @Timed(value = "api.category.update", description = "Time taken to update a category")
    public ResponseEntity<Category> updateCategory (@PathVariable Long id, @RequestBody Category category) {

        logger.debug("REST request to update Category : {}, {}", id, category);

        Category result = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @Timed(value = "api.category.delete", description = "Time taken to delete a category")
    public ResponseEntity<Void> deleteCategory (@PathVariable Long id) {
        logger.debug("REST request to delete Category : {}", id);

        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/parent/{parentId}")
    @Timed(value = "api.category.findByParent", description = "Time taken to find categories by parent")
    public ResponseEntity<List<Category>> getCategoriesByParent (@PathVariable Long parentId) {
        logger.debug("REST request to get Categories by parent : {}", parentId);

        List<Category> categories = categoryService.findByParentCategoryId(parentId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/root")
    @Timed(value = "api.category.findRoot", description = "Time taken to find root categories")
    public ResponseEntity<List<Category>> getRootCategories () {
        logger.debug("REST request to get root Categories");

        List<Category> categories = categoryService.findRootCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/hierarchy")
    @Timed(value = "api.category.hierarchy", description = "Time taken to get category hierarchy")
    public ResponseEntity<List<Category>> getCategoryHierarchy () {
        logger.debug("REST request to get Category hierarchy");

        List<Category> hierarchy = categoryService.getCategoryHierarchy();
        return ResponseEntity.ok(hierarchy);
    }
}
