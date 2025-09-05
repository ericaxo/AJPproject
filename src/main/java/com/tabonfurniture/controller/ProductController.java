package com.tabonfurniture.controller;

import com.tabonfurniture.entity.Product;
import com.tabonfurniture.entity.Review;
import com.tabonfurniture.entity.User;
import com.tabonfurniture.service.ProductService;
import com.tabonfurniture.service.UserService;
import com.tabonfurniture.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
 

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String showProducts(Model model,
                          @RequestParam(value = "category", required = false) String category,
                          @RequestParam(value = "color", required = false) String color,
                          @RequestParam(value = "size", required = false) String size,
                          @RequestParam(required = false) BigDecimal minPrice,
                          @RequestParam(required = false) BigDecimal maxPrice,
                          
                          @RequestParam(value = "search", required = false) String search,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "pageSize", defaultValue = "6") int pageSize,
                          @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
                          @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir,
                          HttpSession session) {
        
        // Server-side validation: Ensure prices are not negative
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            minPrice = null;
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            maxPrice = null;
        }
    
    // Create Pageable object
    Sort sort = sortDir.equalsIgnoreCase("desc") ? 
        Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, pageSize, sort);
    
    Page<Product> productPage;
    
    if (search != null && !search.trim().isEmpty()) {
        productPage = productService.searchProducts(search, pageable);
    } else {
        productPage = productService.getFilteredProducts(category, color, size, minPrice, maxPrice, pageable);
    }
    
    model.addAttribute("productPage", productPage);
    model.addAttribute("products", productPage.getContent());
    model.addAttribute("categories", productService.getAllCategories());
    model.addAttribute("colors", productService.getAllColors());
    model.addAttribute("sizes", productService.getAllDimensions());
    model.addAttribute("selectedCategory", category);
    model.addAttribute("selectedColor", color);
    model.addAttribute("selectedSize", size);
    model.addAttribute("minPrice", minPrice);
    model.addAttribute("maxPrice", maxPrice);
    
    model.addAttribute("search", search);
    model.addAttribute("currentPage", page);
    model.addAttribute("pageSize", pageSize);
    model.addAttribute("sortBy", sortBy);
    model.addAttribute("sortDir", sortDir);
    
    // Fetch recommended products for the logged-in user
    com.tabonfurniture.entity.User user = null;
    Object userObj = session.getAttribute("loggedInUser");
    if (userObj != null && userObj instanceof com.tabonfurniture.dto.UserResponse userResponse) {
        var userOpt = userService.findById(userResponse.getId());
        if (userOpt.isPresent()) {
            user = userOpt.get();
        }
    }
    model.addAttribute("recommendedProducts", productService.getRecommendedProductsForUser(user, 6));
    
    // Add user data for navbar
    model.addAttribute("user", userObj);
    
    return "products/list";
}
    
    @GetMapping("/{id}")
    public String showProductDetails(@PathVariable("id") Long id, Model model, HttpSession session) {
        // Get user session data for navbar
        Object userResponse = session.getAttribute("loggedInUser");
        model.addAttribute("user", userResponse);
        
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "products/details";
        }
        return "redirect:/products";
    }
    
    @GetMapping("/category/{category}")
    public String showProductsByCategory(@PathVariable String category, Model model) {
        List<Product> products = productService.getProductsByCategory(category);
        model.addAttribute("products", products);
        model.addAttribute("category", category);
        return "products/category";
    }
    
    

    // Redirect /product to /products
    @GetMapping("/product")
    public String redirectToProducts() {
        return "redirect:/products";
    }
    
    // Review endpoints
    @GetMapping("/{id}/reviews")
    @ResponseBody
    public Map<String, Object> getReviews(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Review> reviews = productService.getReviewsByProductId(id);
            List<Map<String, Object>> reviewData = reviews.stream()
                .map(review -> {
                    Map<String, Object> reviewMap = new HashMap<>();
                    reviewMap.put("id", review.getId());
                    reviewMap.put("content", review.getContent());
                    reviewMap.put("rating", review.getRating());
                    reviewMap.put("author", review.getUser().getUsername());
                    reviewMap.put("createdAt", review.getCreatedAt());
                    reviewMap.put("authorProfileImage", review.getUser().getProfileImageUrl());
                    return reviewMap;
                })
                .toList();
            
            response.put("success", true);
            response.put("reviews", reviewData);
            response.put("averageRating", productService.getAverageRating(id));
            response.put("reviewCount", productService.getReviewCount(id));
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error loading reviews");
            return response;
        }
    }
    
    @PostMapping("/{id}/review")
    @ResponseBody
    public Map<String, Object> addReview(@PathVariable Long id, 
                                        @RequestParam String content,
                                        @RequestParam Integer rating,
                                        HttpSession session) {
        System.out.println("DEBUG: Received rating value: " + rating);
        System.out.println("DEBUG: Received content: " + content);
        System.out.println("DEBUG: Rating type: " + (rating != null ? rating.getClass().getName() : "null"));
        System.out.println("DEBUG: Rating int value: " + (rating != null ? rating.intValue() : "null"));
        UserResponse userResponse = (UserResponse) session.getAttribute("loggedInUser");
        Map<String, Object> response = new HashMap<>();
        
        if (userResponse == null) {
            response.put("success", false);
            response.put("message", "Please login to review");
            return response;
        }
        
        if (content == null || content.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Review content cannot be empty");
            return response;
        }
        
        if (rating == null || rating < 1 || rating > 5) {
            response.put("success", false);
            response.put("message", "Rating must be between 1 and 5");
            return response;
        }
        
        try {
            Optional<Product> productOpt = productService.getProductById(id);
            if (productOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Product not found");
                return response;
            }
            
            Product product = productOpt.get();
            Optional<User> userOpt = userService.findById(userResponse.getId());
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            User user = userOpt.get();
            
            // Check if user has already reviewed this product
            if (productService.hasUserReviewedProduct(product.getId(), user.getId())) {
                response.put("success", false);
                response.put("message", "You have already reviewed this product. You can edit your existing review instead.");
                return response;
            }
            
            Review review = productService.addReview(product, user, content.trim(), rating);
            int reviewCount = productService.getReviewCount(product.getId());
            Double averageRating = productService.getAverageRating(product.getId());
            
            response.put("success", true);
            response.put("reviewCount", reviewCount);
            response.put("averageRating", averageRating);
            
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("id", review.getId());
            reviewMap.put("content", review.getContent());
            reviewMap.put("rating", review.getRating());
            reviewMap.put("author", review.getUser().getUsername());
            reviewMap.put("createdAt", review.getCreatedAt());
            reviewMap.put("authorProfileImage", review.getUser().getProfileImageUrl());
            response.put("review", reviewMap);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error adding review: " + e.getMessage());
            return response;
        }
    }
    
    @PostMapping("/review/{id}/edit")
    @ResponseBody
    public Map<String, Object> editReview(@PathVariable Long id, 
                                          @RequestBody Map<String, Object> request,
                                          HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("loggedInUser");
        Map<String, Object> response = new HashMap<>();
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "Please login to edit reviews");
            return response;
        }
        
        try {
            String content = (String) request.get("content");
            Integer rating = (Integer) request.get("rating");
            
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Review content cannot be empty");
                return response;
            }
            
            if (rating == null || rating < 1 || rating > 5) {
                response.put("success", false);
                response.put("message", "Rating must be between 1 and 5");
                return response;
            }
            
            Optional<Review> reviewOpt = productService.getReviewById(id);
            if (reviewOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Review not found");
                return response;
            }
            
            Review review = reviewOpt.get();
            
            // Check if user owns this review
            if (!review.getUser().getUsername().equals(user.getUsername())) {
                response.put("success", false);
                response.put("message", "You can only edit your own reviews");
                return response;
            }
            
            review.setContent(content.trim());
            review.setRating(rating);
            productService.saveReview(review);
            
            response.put("success", true);
            response.put("message", "Review updated successfully");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating review: " + e.getMessage());
            return response;
        }
    }
    
    @PostMapping("/review/{id}/delete")
    @ResponseBody
    public Map<String, Object> deleteReview(@PathVariable Long id, HttpSession session) {
        UserResponse user = (UserResponse) session.getAttribute("loggedInUser");
        Map<String, Object> response = new HashMap<>();
        
        if (user == null) {
            response.put("success", false);
            response.put("message", "Please login to delete reviews");
            return response;
        }
        
        try {
            Optional<Review> reviewOpt = productService.getReviewById(id);
            if (reviewOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Review not found");
                return response;
            }
            
            Review review = reviewOpt.get();
            
            // Check if user owns this review
            if (!review.getUser().getUsername().equals(user.getUsername())) {
                response.put("success", false);
                response.put("message", "You can only delete your own reviews");
                return response;
            }
            
            productService.deleteReview(id);
            
            response.put("success", true);
            response.put("message", "Review deleted successfully");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting review: " + e.getMessage());
            return response;
        }
    }
}
