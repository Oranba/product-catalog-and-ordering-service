package com.oranba.springboot.catalog.service.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oranba.springboot.catalog.config.CacheConfig;
import com.oranba.springboot.catalog.domain.model.Category;
import com.oranba.springboot.catalog.repository.CategoryRepository;
import com.oranba.springboot.catalog.service.CategoryService;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final MeterRegistry meterRegistry;

    // Timer metric for performance monitoring
    private final Timer findCategoryTimer;

    @Autowired
    public CategoryServiceImpl (CategoryRepository categoryRepository, MeterRegistry meterRegistry) {
        this.categoryRepository = categoryRepository;
        this.meterRegistry = meterRegistry;

        // Initialize performance metrics
        this.findCategoryTimer = Timer.builder("category.find.time").description("Time taken to find categories").register(meterRegistry);
    }

    @Override
    @Cacheable(value = CacheConfig.CATEGORIES_CACHE, key = "'all'")
    public List<Category> findAllCategories () {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Finding all categories");
            return categoryRepository.findAll();
        }
        finally {
            sample.stop(findCategoryTimer);
        }
    }

    @Override
    @Cacheable(value = CacheConfig.CATEGORIES_CACHE, key = "#id")
    public Optional<Category> findCategoryById (Long id) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Finding category with ID: {}", id);
            return categoryRepository.findById(id);
        }
        finally {
            sample.stop(findCategoryTimer);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CATEGORIES_CACHE, allEntries = true)
    public Category createCategory (Category category) {
        logger.info("Creating new category: {}", category.getName());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CATEGORIES_CACHE, allEntries = true)
    public Category updateCategory (Long id, Category category) {
        logger.info("Updating category with ID: {}", id);

        Optional<Category> existingCategory = categoryRepository.findById(id);

        if (existingCategory.isPresent()) {
            Category categoryToUpdate = existingCategory.get();

            // Update fields
            categoryToUpdate.setName(category.getName());
            categoryToUpdate.setDescription(category.getDescription());
            categoryToUpdate.setParentCategoryId(category.getParentCategoryId());

            return categoryRepository.save(categoryToUpdate);
        }
        else {
            throw new RuntimeException("Category not found with ID: " + id);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CATEGORIES_CACHE, allEntries = true)
    public void deleteCategory (Long id) {
        logger.info("Deleting category with ID: {}", id);
        categoryRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = CacheConfig.CATEGORIES_CACHE, key = "'parent:' + #parentId")
    public List<Category> findByParentCategoryId (Long parentId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Finding categories by parent ID: {}", parentId);
            return categoryRepository.findByParentCategoryId(parentId);
        }
        finally {
            sample.stop(findCategoryTimer);
        }
    }

    @Override
    @Cacheable(value = CacheConfig.CATEGORIES_CACHE, key = "'root'")
    public List<Category> findRootCategories () {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Finding root categories");
            return categoryRepository.findByParentCategoryIdIsNull();
        }
        finally {
            sample.stop(findCategoryTimer);
        }
    }

    @Override
    @Cacheable(value = CacheConfig.CATEGORIES_CACHE, key = "'hierarchy'")
    public List<Category> getCategoryHierarchy () {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            logger.debug("Getting category hierarchy");
            return categoryRepository.findAllWithHierarchy();
        }
        finally {
            sample.stop(findCategoryTimer);
        }
    }
}
