package com.oranba.springboot.catalog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.oranba.springboot.catalog.domain.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find categories by parent category id
    List<Category> findByParentCategoryId (Long parentId);

    // Find all root categories (those without a parent)
    List<Category> findByParentCategoryIdIsNull ();

    // Find categories by name containing the search term
    List<Category> findByNameContainingIgnoreCase (String name);

    // Custom query to get category hierarchy
    @Query("SELECT c FROM Category c LEFT JOIN FETCH Category p ON c.parentCategoryId = p.id ORDER BY COALESCE(c.parentCategoryId, 0), c.name")
    List<Category> findAllWithHierarchy ();
}
