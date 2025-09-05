package com.tabonfurniture.repository;

import com.tabonfurniture.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByProductIdAndIsActiveOrderByCreatedAtDesc(Long productId, Boolean isActive);
    
    List<Review> findByProductId(Long productId);
    
    Long countByProductIdAndIsActive(Long productId, Boolean isActive);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isActive = :isActive")
    Double getAverageRatingByProductIdAndIsActive(@Param("productId") Long productId, @Param("isActive") Boolean isActive);
    
    void deleteByProductId(Long productId);
    
    void deleteByUserId(Long userId);
    
    // Check if user has already reviewed a product
    boolean existsByProductIdAndUserIdAndIsActive(Long productId, Long userId, Boolean isActive);
    
    // Get user's review for a specific product
    Optional<Review> findByProductIdAndUserIdAndIsActive(Long productId, Long userId, Boolean isActive);
}
