package com.tabonfurniture.repository;

import com.tabonfurniture.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    List<Category> findByIsActiveTrue();
    
    List<Category> findByIsActive(Boolean isActive);
    
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.name")
    List<Category> findAllActiveCategories();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :categoryName")
    Long countProductsByCategoryName(String categoryName);
    
    boolean existsByName(String name);
} 