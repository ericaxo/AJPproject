package com.AuroraDecors.service;

import com.AuroraDecors.entity.Category;
import com.AuroraDecors.entity.Product;
import com.AuroraDecors.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }
    
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
    
    public boolean categoryExists(String name) {
        return categoryRepository.existsByName(name);
    }
    
    public Long getProductCountByCategory(String categoryName) {
        return categoryRepository.countProductsByCategoryName(categoryName);
    }
    
    public void populateProductCounts(List<Category> categories) {
        for (Category category : categories) {
            Long count = getProductCountByCategory(category.getName());
            // Set the count using reflection since we can't modify the entity easily
            try {
                java.lang.reflect.Field productsField = Category.class.getDeclaredField("products");
                productsField.setAccessible(true);
                List<Product> products = new ArrayList<>();
                // Create a dummy product list with the count
                for (int i = 0; i < count; i++) {
                    products.add(new Product());
                }
                productsField.set(category, products);
            } catch (Exception e) {
                // If reflection fails, just continue
                System.out.println("Could not populate product count for category: " + category.getName());
            }
        }
    }
    
    public Category createCategory(String name, String description, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIcon(icon);
        category.setIsActive(true);
        return categoryRepository.save(category);
    }
} 