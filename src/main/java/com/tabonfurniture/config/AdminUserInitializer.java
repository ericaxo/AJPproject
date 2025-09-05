package com.tabonfurniture.config;

import com.tabonfurniture.entity.User;
import com.tabonfurniture.repository.UserRepository;
import com.tabonfurniture.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Run first
public class AdminUserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin user exists
        if (!userRepository.existsByEmail("admin@tabonfurniture.com")) {
            System.out.println("Creating default admin user...");
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@tabonfurniture.com");
            adminUser.setPassword(PasswordUtil.hashPassword("admin123"));
            adminUser.setRole(User.Role.ADMIN);
            
            userRepository.save(adminUser);
            
            System.out.println("✅ Default admin user created successfully!");
            System.out.println("📧 Email: admin@tabonfurniture.com");
            System.out.println("🔑 Password: admin123");
        } else {
            System.out.println("✅ Admin user already exists in database");
        }
    }
} 