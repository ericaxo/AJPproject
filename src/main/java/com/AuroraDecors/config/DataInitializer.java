package com.AuroraDecors.config;

import com.AuroraDecors.entity.Category;
import com.AuroraDecors.entity.Product;
import com.AuroraDecors.entity.User;
import com.AuroraDecors.repository.CartItemRepository;
import com.AuroraDecors.repository.CartRepository;
import com.AuroraDecors.repository.CategoryRepository;
import com.AuroraDecors.repository.OrderItemRepository;
import com.AuroraDecors.repository.OrderRepository;
import com.AuroraDecors.repository.ProductRepository;
import com.AuroraDecors.repository.ReviewRepository;
import com.AuroraDecors.repository.UserRepository;
import com.AuroraDecors.util.PasswordUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

 
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@Order(2) // Run after AdminUserInitializer
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.seed.furniture.reset:false}")
    private boolean resetFurniture;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Autowired(required = false)
    private OrderItemRepository orderItemRepository;

    @Autowired(required = false)
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private ReviewRepository reviewRepository;

    @Autowired(required = false)
    private CartItemRepository cartItemRepository;

    @Autowired(required = false)
    private CartRepository cartRepository;

    @Override
    public void run(String... args) throws Exception {
        if (!seedEnabled) {
            System.out.println("\u23f8\ufe0f Seeding disabled (app.seed.enabled=false). Skipping data initialization.");
            return;
        }

        if (resetFurniture) {
            System.out.println("⚠️ app.seed.furniture.reset=true detected. Dropping and reseeding furniture data...");
            reseedFurnitureData();
            System.out.println("✅ Furniture reseed complete.");
            return;
        }
        // Create sample categories if none exist
        if (categoryRepository.count() == 0) {
            System.out.println("Creating sample categories...");
            createSampleCategories();
            System.out.println("✅ Sample categories created successfully!");
        } else {
            System.out.println("✅ Categories already exist in database");
        }

        // Create sample products if none exist
        if (productRepository.count() == 0) {
            System.out.println("Creating sample products...");
            createSampleProducts();
            System.out.println("✅ Sample products created successfully!");
        } else {
            System.out.println("✅ Products already exist in database");
            // Update stock quantities for existing products
            System.out.println("🔄 Updating stock quantities for existing products...");
            updateStockQuantities();
            System.out.println("✅ Stock quantities updated successfully!");
            // Fix any incorrect seeded images (non-furniture)
            fixIncorrectImages();
        }

        // Create sample regular users if none exist (excluding admin)
        if (userRepository.count() <= 1) { // Only admin exists
            System.out.println("Creating sample users...");
            createSampleUsers();
            System.out.println("✅ Sample users created successfully!");
        } else {
            System.out.println("✅ Users already exist in database");
        }
    }



    private void createSampleProducts() {
        List<Product> products = Arrays.asList(
            createProduct("Modern Fabric Sofa", "Comfortable 3-seater sofa with durable fabric upholstery", 
                new BigDecimal("699.00"), "Living Room", "3-Seater", "Gray", "All Season", "Fabric & Wood", 40, "https://images.unsplash.com/photo-1501045661006-fcebe0257c3f?w=400&h=400&fit=crop"),
            createProduct("Solid Wood Coffee Table", "Minimalist coffee table made of solid oak wood", 
                new BigDecimal("249.00"), "Living Room", "120x60x45 cm", "Walnut", "All Season", "Solid Wood", 60, "https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=400&h=400&fit=crop"),
            createProduct("Queen Size Bed Frame", "Sturdy queen-size bed frame with headboard", 
                new BigDecimal("499.00"), "Bedroom", "Queen", "Natural", "All Season", "Wood & Veneer", 30, "https://images.unsplash.com/photo-1505691723518-36a5ac3b2bb3?w=400&h=400&fit=crop"),
            createProduct("Memory Foam Mattress", "Medium-firm 8-inch memory foam mattress", 
                new BigDecimal("399.00"), "Bedroom", "Queen", "White", "All Season", "Memory Foam", 50, "https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=400&h=400&fit=crop"),
            createProduct("Extendable Dining Table", "Extendable dining table seats up to 8 people", 
                new BigDecimal("549.00"), "Dining", "160-210x90x75 cm", "Oak", "All Season", "Engineered Wood", 20, "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=400&h=400&fit=crop"),
            createProduct("Ergonomic Office Chair", "Adjustable lumbar support and breathable mesh back", 
                new BigDecimal("199.00"), "Office", "Standard", "Black", "All Season", "Mesh & Metal", 80, "https://images.unsplash.com/photo-1582582621959-48d233a81f49?w=400&h=400&fit=crop"),
            createProduct("Outdoor Lounge Set", "Weather-resistant lounge set with cushions", 
                new BigDecimal("899.00"), "Outdoor", "4-Piece", "Beige", "All Season", "Rattan & Aluminum", 15, "https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=400&h=400&fit=crop"),
            createProduct("Bookshelf with Cabinets", "Tall bookshelf with lower storage cabinets", 
                new BigDecimal("279.00"), "Storage", "90x35x200 cm", "Walnut", "All Season", "Engineered Wood", 40, "https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=400&h=400&fit=crop"),
            createProduct("Kids Study Desk", "Height-adjustable study desk for kids", 
                new BigDecimal("159.00"), "Kids", "Small", "White", "All Season", "MDF & Steel", 50, "https://images.unsplash.com/photo-1519710164239-da123dc03ef4?w=400&h=400&fit=crop"),
            createProduct("Decorative Floor Lamp", "Modern floor lamp with fabric shade", 
                new BigDecimal("89.00"), "Decor", "Standard", "Black", "All Season", "Metal & Fabric", 120, "https://images.unsplash.com/photo-1481277542470-605612bd2d61?w=400&h=400&fit=crop")
        );

        productRepository.saveAll(products);
        System.out.println("✅ Created " + products.size() + " products with stock quantities ranging from 50 to 300 units!");
    }

    private void reseedFurnitureData() {
        // Clear dependent data first to satisfy FK constraints
        try {
            if (reviewRepository != null) reviewRepository.deleteAll();
        } catch (Exception ignored) {}
        try {
            if (cartItemRepository != null) cartItemRepository.deleteAll();
        } catch (Exception ignored) {}
        try {
            if (cartRepository != null) cartRepository.deleteAll();
        } catch (Exception ignored) {}
        try {
            if (orderItemRepository != null) orderItemRepository.deleteAll();
        } catch (Exception ignored) {}
        try {
            if (orderRepository != null) orderRepository.deleteAll();
        } catch (Exception ignored) {}
        
        // Now clear products and categories
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Reseed furniture
        createSampleCategories();
        createSampleProducts();
    }
    
    private void fixIncorrectImages() {
        List<Product> allProducts = productRepository.findAll();
        int updates = 0;
        for (Product p : allProducts) {
            String url = p.getImageUrl();
            if (url != null && url.contains("photo-1493666438817-866a91353ca9")) {
                p.setImageUrl("https://images.unsplash.com/photo-1481277542470-605612bd2d61?w=400&h=400&fit=crop");
                updates++;
            }
        }
        if (updates > 0) {
            productRepository.saveAll(allProducts);
            System.out.println("✅ Updated " + updates + " product images to furniture images");
        }
    }
    
    private void updateStockQuantities() {
        List<Product> allProducts = productRepository.findAll();
        System.out.println("Found " + allProducts.size() + " products to update stock quantities");
        
        for (Product product : allProducts) {
            // Update stock quantities based on product type and current quantity
            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            int newStock = calculateNewStockQuantity(product, currentStock);
            
            if (newStock != currentStock) {
                product.setStockQuantity(newStock);
                System.out.println("Updated " + product.getName() + " stock from " + currentStock + " to " + newStock);
            }
        }
        
        productRepository.saveAll(allProducts);
        System.out.println("✅ Updated stock quantities for " + allProducts.size() + " products");
    }
    
    private int calculateNewStockQuantity(Product product, int currentStock) {
        String category = product.getCategory() != null ? product.getCategory().toLowerCase() : "";
        String name = product.getName() != null ? product.getName().toLowerCase() : "";
        
        // Base stock quantities by category
        if (category.contains("accessories") || name.contains("socks") || name.contains("belt") || name.contains("scarf")) {
            return Math.max(currentStock, 150 + (int)(Math.random() * 150)); // 150-300
        } else if (category.contains("shoes") || category.contains("footwear")) {
            return Math.max(currentStock, 50 + (int)(Math.random() * 50)); // 50-100
        } else if (category.contains("tops") || category.contains("bottoms")) {
            return Math.max(currentStock, 75 + (int)(Math.random() * 75)); // 75-150
        } else if (category.contains("dresses")) {
            return Math.max(currentStock, 60 + (int)(Math.random() * 40)); // 60-100
        } else if (category.contains("outerwear")) {
            return Math.max(currentStock, 70 + (int)(Math.random() * 30)); // 70-100
        } else {
            return Math.max(currentStock, 100 + (int)(Math.random() * 100)); // 100-200
        }
    }

    private void createSampleUsers() {
        List<User> users = Arrays.asList(
            createUser("john_doe", "user@gmail.com", "user123"),
            createUser("jane_smith", "jane@example.com", "user123"),
            createUser("eco_lover", "eco@example.com", "user123")
        );

        userRepository.saveAll(users);
    }

    private Product createProduct(String name, String description, BigDecimal price, String category,
                                  String dimensions, String color, String season, String material,
                                  int stockQuantity, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setDimensions(dimensions);
        product.setColor(color);
        product.setMaterial(material);
        product.setStockQuantity(stockQuantity);
        product.setImageUrl(imageUrl);
        product.setIsActive(true);
        return product;
    }

    private void createSampleCategories() {
        List<Category> categories = Arrays.asList(
            createCategory("Living Room", "Sofas, sectionals, coffee tables, and TV units for living spaces", "fas fa-couch"),
            createCategory("Bedroom", "Beds, mattresses, wardrobes, and bedside tables", "fas fa-bed"),
            createCategory("Dining", "Dining tables, chairs, sideboards, and bar stools", "fas fa-utensils"),
            createCategory("Office", "Desks, office chairs, bookcases, and storage", "fas fa-briefcase"),
            createCategory("Outdoor", "Patio sets, loungers, and outdoor accessories", "fas fa-umbrella-beach"),
            createCategory("Storage", "Cabinets, shelves, and organizers", "fas fa-box"),
            createCategory("Kids", "Furniture designed for children and nurseries", "fas fa-child"),
            createCategory("Decor", "Rugs, lamps, mirrors, and decor accents", "fas fa-lightbulb")
        );

        categoryRepository.saveAll(categories);
    }

    private Category createCategory(String name, String description, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIcon(icon);
        category.setIsActive(true);
        return category;
    }

    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(PasswordUtil.hashPassword(password));
        user.setRole(User.Role.USER);
        return user;
    }
} 