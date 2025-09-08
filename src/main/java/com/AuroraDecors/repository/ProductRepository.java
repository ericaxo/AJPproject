package com.AuroraDecors.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.AuroraDecors.entity.Product;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByCategory(String category);
    
    List<Product> findByColor(String color);
    
    List<Product> findByDimensions(String dimensions);
    
    List<Product> findByMaterial(String material);
    
    
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    List<Product> findInStockProducts();
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    Page<Product> findByCategory(String category, Pageable pageable);
    
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL")
    List<String> findAllCategories();
    
    @Query("SELECT DISTINCT p.color FROM Product p WHERE p.color IS NOT NULL")
    List<String> findAllColors();
    
    @Query("SELECT DISTINCT p.dimensions FROM Product p WHERE p.dimensions IS NOT NULL")
    List<String> findAllDimensions();

    @Query("SELECT p FROM Product p WHERE " +
            "(:category IS NULL OR :category = '' OR p.category = :category) AND " +
            "(:color IS NULL OR :color = '' OR p.color = :color) AND " +
            "(:size IS NULL OR :size = '' OR p.dimensions = :size) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findFilteredProducts(@Param("category") String category,
                                      @Param("color") String color,
                                      @Param("size") String size,
                                      @Param("minPrice") BigDecimal minPrice,
                                      @Param("maxPrice") BigDecimal maxPrice,
                                      Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.imageUrl IS NULL OR p.imageUrl = '' OR p.imageUrl = 'null'")
    List<Product> findByImageUrlIsNullOrImageUrlIsEmptyOrImageUrlEquals();
    
    // Get recent products (top 5 by creation date)
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC LIMIT 5")
    List<Product> findTop5ByOrderByCreatedAtDesc();
    
    // Get most recent product (top 1 by creation date)
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC LIMIT 1")
    List<Product> findTop1ByOrderByCreatedAtDesc();
}
