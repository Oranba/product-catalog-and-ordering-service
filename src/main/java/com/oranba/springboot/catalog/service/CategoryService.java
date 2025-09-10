package com.oranba.springboot.catalog.service;

import java.util.List;
import java.util.Optional;

import com.oranba.springboot.catalog.domain.model.Category;

public interface CategoryService {

    /**
     * Find all categories
     * 
     * @return List of all categories
     */
    List<Category> findAllCategories ();

    /**
     * Find a category by its ID
     * 
     * @param id
     *        Category ID
     * @return Optional containing the category if found
     */
    Optional<Category> findCategoryById (Long id);

    /**
     * Create a new category
     * 
     * @param category
     *        Category to create
     * @return Created category
     */
    Category createCategory (Category category);

    /**
     * Update an existing category
     * 
     * @param id
     *        Category ID
     * @param category
     *        Updated category data
     * @return Updated category
     */
    Category updateCategory (Long id, Category category);

    /**
     * Delete a category
     * 
     * @param id
     *        Category ID
     */
    void deleteCategory (Long id);

    /**
     * Find categories by parent category ID
     * 
     * @param parentId
     *        Parent category ID
     * @return List of child categories
     */
    List<Category> findByParentCategoryId (Long parentId);

    /**
     * Find all root categories (those without a parent)
     * 
     * @return List of root categories
     */
    List<Category> findRootCategories ();

    /**
     * Get the complete category hierarchy
     * 
     * @return List of all categories ordered in a hierarchy
     */
    List<Category> getCategoryHierarchy ();
}
