package com.tabonfurniture.service;

import com.tabonfurniture.entity.Order;
import com.tabonfurniture.entity.Product;
import com.tabonfurniture.repository.OrderRepository;
import com.tabonfurniture.repository.ProductRepository;
import com.tabonfurniture.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get total users count
            long totalUsers = userRepository.count();
            stats.put("totalUsers", totalUsers);
            
            // Get total products count
            long totalProducts = productRepository.count();
            stats.put("totalProducts", totalProducts);
            
            // Get total orders count
            long totalOrders = orderRepository.count();
            stats.put("totalOrders", totalOrders);
            
            // Calculate total revenue (sum of all paid orders)
            List<Order> paidOrders = orderRepository.findByPaymentStatus(Order.PaymentStatus.PAID);
            BigDecimal totalRevenue = paidOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRevenue", totalRevenue);
            
            // Get pending orders count
            long pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING).size();
            stats.put("pendingOrders", pendingOrders);
            
            // Get recent orders (last 1)
            List<Order> recentOrders = orderRepository.findTop1ByOrderByCreatedAtDesc();
            stats.put("recentOrders", recentOrders);
            
            // Get recent products (last 1)
            List<Product> recentProducts = productRepository.findTop1ByOrderByCreatedAtDesc();
            stats.put("recentProducts", recentProducts);
            
            // Calculate percentage changes (mock data for now)
            stats.put("userGrowth", 12);
            stats.put("orderGrowth", 8);
            stats.put("productGrowth", 5);
            stats.put("revenueGrowth", 10);
            
            // Debug output
            System.out.println("Dashboard Stats: " + stats);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error calculating dashboard stats: " + e.getMessage());
            e.printStackTrace();
            
            // Provide default values
            stats.put("totalUsers", 0L);
            stats.put("totalProducts", 0L);
            stats.put("totalOrders", 0L);
            stats.put("totalRevenue", BigDecimal.ZERO);
            stats.put("pendingOrders", 0L);
            stats.put("recentOrders", new ArrayList<>());
            stats.put("recentProducts", new ArrayList<>());
            stats.put("userGrowth", 0);
            stats.put("orderGrowth", 0);
            stats.put("productGrowth", 0);
            stats.put("revenueGrowth", 0);
        }
        
        return stats;
    }
    
    public long getTotalUsers() {
        return userRepository.count();
    }
    
    public long getTotalProducts() {
        return productRepository.count();
    }
    
    public long getTotalOrders() {
        return orderRepository.count();
    }
    
    public BigDecimal getTotalRevenue() {
        List<Order> paidOrders = orderRepository.findByPaymentStatus(Order.PaymentStatus.PAID);
        return paidOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public long getPendingOrdersCount() {
        return orderRepository.findByStatus(Order.OrderStatus.PENDING).size();
    }
}
