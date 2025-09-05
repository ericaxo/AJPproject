package com.tabonfurniture.service;

import com.tabonfurniture.entity.*;
import com.tabonfurniture.repository.OrderRepository;
import com.tabonfurniture.repository.OrderItemRepository;
import com.tabonfurniture.repository.CartItemRepository;
import com.tabonfurniture.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private CartRepository cartRepository;
    
    @Transactional
    public Order createOrderFromCart(User user, String shippingAddress) {
        // Get cart
        Optional<Cart> cartOpt = cartRepository.findByUser(user);
        if (!cartOpt.isPresent()) {
            throw new RuntimeException("Cart not found for user");
        }
        
        Cart cart = cartOpt.get();
        
        // Get cart items with fresh query
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot create order from empty cart");
        }
        
        // Calculate total from cart items
        BigDecimal totalAmount = cartItems.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Create order
        Order order = new Order(user, totalAmount, shippingAddress);
        order = orderRepository.save(order);
        
        // Create order items from cart items (using data, not entities)
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItemRepository.save(orderItem);
        }
        
        // Clear cart items
        cartItemRepository.deleteByCart(cart);
        
        // Update cart total
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);
        
        return order;
    }
    
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }
    
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByDateDesc(userId);
    }
    
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAllOrdersByDateDesc();
    }
    
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found");
    }
    
    public Order updatePaymentStatus(Long orderId, Order.PaymentStatus paymentStatus) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setPaymentStatus(paymentStatus);
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found");
    }
    
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public void cancelOrder(Order order) {
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
    
    public int getOrderCountByUserId(Long userId) {
        return orderRepository.countByUserId(userId).intValue();
    }
    
    public double getTotalSpentByUserId(Long userId) {
        List<Order> userOrders = orderRepository.findByUserIdOrderByDateDesc(userId);
        return userOrders.stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
    }

    public List<Long> getMostOrderedProductIdsByUser(User user, int limit) {
        List<Order> orders = getOrdersByUser(user);
        Map<Long, Integer> productCount = new HashMap<>();
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            for (OrderItem item : items) {
                Long productId = item.getProduct().getId();
                productCount.put(productId, productCount.getOrDefault(productId, 0) + item.getQuantity());
            }
        }
        return productCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getMostOrderedCategoriesByUser(User user, int limit) {
        List<Order> orders = getOrdersByUser(user);
        Map<String, Integer> categoryCount = new HashMap<>();
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            for (OrderItem item : items) {
                String category = item.getProduct().getCategory();
                if (category != null) {
                    categoryCount.put(category, categoryCount.getOrDefault(category, 0) + item.getQuantity());
                }
            }
        }
        return categoryCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }
}
