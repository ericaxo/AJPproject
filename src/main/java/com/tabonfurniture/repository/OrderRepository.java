package com.tabonfurniture.repository;

import com.tabonfurniture.entity.Order;
import com.tabonfurniture.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUser(User user);
    
    List<Order> findByUserId(Long userId);
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    List<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus);
    
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findAllOrdersByDateDesc();
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserIdOrderByDateDesc(Long userId);
    
    Long countByUserId(Long userId);
    
    // Delete orders by user ID
    void deleteByUserId(Long userId);
    
    // Get recent orders (top 5 by order date)
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC LIMIT 5")
    List<Order> findTop5ByOrderByCreatedAtDesc();
    
    // Get most recent order (top 1 by order date)
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC LIMIT 1")
    List<Order> findTop1ByOrderByCreatedAtDesc();
}
