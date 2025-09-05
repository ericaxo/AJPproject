package com.tabonfurniture.service;

import com.tabonfurniture.entity.Product;
import com.tabonfurniture.entity.Order;
import com.tabonfurniture.entity.Review;
 
import com.tabonfurniture.repository.ProductRepository;
import com.tabonfurniture.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.tabonfurniture.entity.User;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ReviewRepository reviewRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    
    
    public List<Product> getInStockProducts() {
        return productRepository.findInStockProducts();
    }
    
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }
    
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }
    
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }
    
    public List<String> getAllColors() {
        return productRepository.findAllColors();
    }
    
    public List<String> getAllDimensions() {
        return productRepository.findAllDimensions();
    }
    
    public List<Product> getFilteredProducts(String category, String color, String size, 
                                           BigDecimal minPrice, BigDecimal maxPrice) {
        List<Product> products = productRepository.findAll();
        
        return products.stream()
            .filter(p -> category == null || category.equals(p.getCategory()))
            .filter(p -> color == null || color.equals(p.getColor()))
            .filter(p -> size == null || size.equals(p.getDimensions()))
            .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
            .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
            .toList();
    }

    public Page<Product> getFilteredProducts(String category, String color, String size,
                                               BigDecimal minPrice, BigDecimal maxPrice,
                                               Pageable pageable) {
        return productRepository.findFilteredProducts(category, color, size, minPrice, maxPrice, pageable);
    }

    public List<Product> getRecommendedProductsForUser(User user, int limit) {
        // If user is not logged in, return random products that change over time
        if (user == null) {
            return getRandomProductsWithTimeSeed(limit);
        }
        
        // Check if user has order history
        List<Order> userOrders = orderService.getOrdersByUser(user);
        
        // If user has no orders, return random products with time-based changes
        if (userOrders.isEmpty()) {
            return getRandomProductsWithUserSeed(user.getId(), limit);
        }
        
        // For users with order history, provide personalized recommendations
        return getPersonalizedRecommendations(user, limit);
    }
    
    private List<Product> getRandomProductsWithTimeSeed(int limit) {
        List<Product> allProducts = productRepository.findAll();
        if (allProducts.size() <= limit) {
            return allProducts;
        }
        
        // Use current time to create a seed that changes every hour
        long timeSeed = System.currentTimeMillis() / (1000 * 60 * 60); // Changes every hour
        java.util.Random random = new java.util.Random(timeSeed);
        
        List<Product> shuffled = new java.util.ArrayList<>(allProducts);
        java.util.Collections.shuffle(shuffled, random);
        
        return shuffled.subList(0, limit);
    }
    
    private List<Product> getRandomProductsWithUserSeed(Long userId, int limit) {
        List<Product> allProducts = productRepository.findAll();
        if (allProducts.size() <= limit) {
            return allProducts;
        }
        
        // Use user ID and time to create a seed that changes every 2 hours
        long timeSeed = System.currentTimeMillis() / (1000 * 60 * 60 * 2); // Changes every 2 hours
        long userSeed = userId != null ? userId : 0;
        java.util.Random random = new java.util.Random(timeSeed + userSeed);
        
        List<Product> shuffled = new java.util.ArrayList<>(allProducts);
        java.util.Collections.shuffle(shuffled, random);
        
        return shuffled.subList(0, limit);
    }
    
    private List<Product> getPersonalizedRecommendations(User user, int limit) {
        // Get user's order history
        List<Long> mostOrderedProductIds = orderService.getMostOrderedProductIdsByUser(user, 10);
        List<String> mostOrderedCategories = orderService.getMostOrderedCategoriesByUser(user, 5);
        
        List<Product> recommended = new java.util.ArrayList<>();
        List<Long> alreadyBought = new java.util.ArrayList<>(mostOrderedProductIds);
        
        // Strategy 1: Recommend similar products from most ordered categories
        for (String category : mostOrderedCategories) {
            List<Product> categoryProducts = getProductsByCategory(category);
            List<Product> availableProducts = categoryProducts.stream()
                .filter(p -> !alreadyBought.contains(p.getId()))
                .toList();
            
            if (!availableProducts.isEmpty()) {
                // Add some randomness to category-based recommendations
                long timeSeed = System.currentTimeMillis() / (1000 * 60 * 30); // Changes every 30 minutes
                java.util.Random random = new java.util.Random(timeSeed + user.getId());
                List<Product> shuffled = new java.util.ArrayList<>(availableProducts);
                java.util.Collections.shuffle(shuffled, random);
                
                for (Product p : shuffled) {
                    if (recommended.size() < limit * 2 / 3) { // Use 2/3 of limit for category-based
                        recommended.add(p);
                    }
                }
            }
        }
        
        // Strategy 2: Add some diversity with random products from other categories
        List<Product> allProducts = productRepository.findAll();
        List<Product> otherProducts = allProducts.stream()
            .filter(p -> !mostOrderedCategories.contains(p.getCategory()))
            .filter(p -> !alreadyBought.contains(p.getId()))
            .filter(p -> !recommended.contains(p))
            .toList();
        
        if (!otherProducts.isEmpty()) {
            long timeSeed = System.currentTimeMillis() / (1000 * 60 * 45); // Changes every 45 minutes
            java.util.Random random = new java.util.Random(timeSeed + user.getId());
            List<Product> shuffled = new java.util.ArrayList<>(otherProducts);
            java.util.Collections.shuffle(shuffled, random);
            
            for (Product p : shuffled) {
                if (recommended.size() < limit) {
                    recommended.add(p);
                }
            }
        }
        
        // Strategy 3: Fill remaining slots with popular products
        if (recommended.size() < limit) {
            List<Product> popular = getPopularProducts(limit * 2);
            for (Product p : popular) {
                if (!alreadyBought.contains(p.getId()) && !recommended.contains(p) && recommended.size() < limit) {
                    recommended.add(p);
                }
            }
        }
        
        // Final shuffle to add more randomness
        long finalSeed = System.currentTimeMillis() / (1000 * 60 * 15); // Changes every 15 minutes
        java.util.Random finalRandom = new java.util.Random(finalSeed + user.getId());
        java.util.Collections.shuffle(recommended, finalRandom);
        
        return recommended.subList(0, Math.min(limit, recommended.size()));
    }

    public List<Product> getPopularProducts(int limit) {
        // Fallback: just return most ordered or most reviewed products, or latest
        return productRepository.findAll().stream()
                .sorted((a, b) -> b.getOrderItems().size() - a.getOrderItems().size())
                .limit(limit)
                .toList();
    }
    
    // Review functionality
    public boolean hasUserReviewedProduct(Long productId, Long userId) {
        return reviewRepository.existsByProductIdAndUserIdAndIsActive(productId, userId, true);
    }
    
    public Optional<Review> getUserReviewForProduct(Long productId, Long userId) {
        return reviewRepository.findByProductIdAndUserIdAndIsActive(productId, userId, true);
    }
    
    public Review addReview(Product product, User user, String content, Integer rating) {
        // Check if user has already reviewed this product
        if (hasUserReviewedProduct(product.getId(), user.getId())) {
            throw new IllegalStateException("You have already reviewed this product");
        }
        
        Review review = new Review(content, rating, product, user);
        return reviewRepository.save(review);
    }
    
    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductIdAndIsActiveOrderByCreatedAtDesc(productId, true);
    }
    
    public int getReviewCount(Long productId) {
        return reviewRepository.countByProductIdAndIsActive(productId, true).intValue();
    }
    
    public Double getAverageRating(Long productId) {
        Double avgRating = reviewRepository.getAverageRatingByProductIdAndIsActive(productId, true);
        return avgRating != null ? avgRating : 0.0;
    }
    
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }
    
    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }
    
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}
