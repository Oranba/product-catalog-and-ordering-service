package com.oranba.springboot.catalog.config;

import java.util.Arrays;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    // Define cache regions
    public static final String PRODUCTS_CACHE = "products";
    public static final String PRODUCT_DETAILS_CACHE = "productDetails";
    public static final String CATEGORIES_CACHE = "categories";

    @Bean
    public CacheManager cacheManager () {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Arrays.asList(PRODUCTS_CACHE, PRODUCT_DETAILS_CACHE, CATEGORIES_CACHE));
        return cacheManager;
    }
}
