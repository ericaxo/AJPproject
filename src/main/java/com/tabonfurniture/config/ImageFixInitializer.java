package com.tabonfurniture.config;

import com.tabonfurniture.entity.Product;
import com.tabonfurniture.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3) // Run after DataInitializer
public class ImageFixInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔧 Checking for products without images...");
        
        List<Product> productsWithoutImages = productRepository.findByImageUrlIsNullOrImageUrlIsEmptyOrImageUrlEquals();
        
        if (!productsWithoutImages.isEmpty()) {
            System.out.println("Found " + productsWithoutImages.size() + " products without images. Fixing...");
            
            String defaultImageUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=400&fit=crop";
            
            for (Product product : productsWithoutImages) {
                System.out.println("Fixing product: " + product.getName() + " (ID: " + product.getId() + ")");
                product.setImageUrl(defaultImageUrl);
                productRepository.save(product);
            }
            
            System.out.println("✅ Fixed " + productsWithoutImages.size() + " products with default images!");
        } else {
            System.out.println("✅ All products have images!");
        }
    }
} 