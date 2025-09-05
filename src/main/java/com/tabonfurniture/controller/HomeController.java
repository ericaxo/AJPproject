package com.tabonfurniture.controller;

import com.tabonfurniture.entity.Cart;
import com.tabonfurniture.entity.Contact;
import com.tabonfurniture.entity.Order;
import com.tabonfurniture.entity.Product;
import com.tabonfurniture.entity.User;
import com.tabonfurniture.service.AdminService;
import com.tabonfurniture.service.CartService;
import com.tabonfurniture.service.ContactService;
import com.tabonfurniture.service.OrderService;
import com.tabonfurniture.service.ProductService;
import com.tabonfurniture.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ContactService contactService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        // Get user session data for navbar
        Object userResponse = session.getAttribute("loggedInUser");
        model.addAttribute("user", userResponse);
        
        // Get featured products from backend (first 4 active products)
        List<Product> featuredProducts = productService.getAllProducts().stream()
            .filter(product -> product.getIsActive() != null && product.getIsActive())
            .limit(4)
            .toList();
        
        model.addAttribute("featuredProducts", featuredProducts);
        return "index";
    }
    
    @GetMapping("/shop")
    public String shop(Model model) {
        return "redirect:/products";
    }
    
    @GetMapping("/about")
    public String about(Model model, HttpSession session) {
        // Get user session data for navbar
        Object userResponse = session.getAttribute("loggedInUser");
        model.addAttribute("user", userResponse);
        return "about";
    }
    
    @GetMapping("/contact")
    public String contact(Model model, HttpSession session) {
        // Get user session data for navbar
        Object userResponse = session.getAttribute("loggedInUser");
        model.addAttribute("user", userResponse);
        
        // If user is logged in, pre-fill the form with their data
        if (userResponse != null) {
            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) {
                userService.findById(userId).ifPresent(user -> {
                    model.addAttribute("userEmail", user.getEmail());
                    model.addAttribute("userName", user.getUsername());
                });
            }
        }
        
        return "contact";
    }
    
    @PostMapping("/contact")
    public String submitContact(@RequestParam(value = "name") String name,
                            @RequestParam(value = "email") String email,
                            @RequestParam(value = "subject") String subject,
                            @RequestParam(value = "message") String message,
                           Model model, HttpSession session) {
        // Get user session data for navbar
        Object userResponse = session.getAttribute("loggedInUser");
        model.addAttribute("user", userResponse);
        
        try {
            // Create contact object
            Contact contact = new Contact(name, email, subject, message);
            
            // If user is logged in, associate the contact with the user
            if (userResponse != null) {
                Long userId = (Long) session.getAttribute("userId");
                if (userId != null) {
                    userService.findById(userId).ifPresent(user -> {
                        contact.setUser(user);
                        model.addAttribute("userEmail", user.getEmail());
                        model.addAttribute("userName", user.getUsername());
                    });
                }
            }
            
            // Save the contact
            contactService.saveContact(contact);
            
            model.addAttribute("successMessage", "Thank you for your message! We'll get back to you soon.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Sorry, there was an error sending your message. Please try again.");
        }
        
        return "contact";
    }
    

    
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }
        
        Long userId = (Long) session.getAttribute("userId");
        
        // Get cart information
        Cart cart = cartService.getCartByUserId(userId);
        int cartItemCount = cart != null ? cart.getTotalItems() : 0;
        
        // Get order information
        List<Order> orders = orderService.getOrdersByUserId(userId);
        int orderCount = orders.size();
        
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("orderCount", orderCount);
        return "index";
    }
    
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        Object userRole = session.getAttribute("userRole");
        
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }
        
        if (userRole == null || !userRole.toString().equals("ADMIN")) {
            return "redirect:/dashboard";
        }
        
        try {
            // Get dashboard statistics
            Map<String, Object> stats = adminService.getDashboardStats();
            
            // Ensure stats is not null
            if (stats == null) {
                stats = new HashMap<>();
                stats.put("totalUsers", 0L);
                stats.put("totalProducts", 0L);
                stats.put("totalOrders", 0L);
                stats.put("totalRevenue", BigDecimal.ZERO);
                stats.put("pendingOrders", 0L);
            }
            
            model.addAttribute("stats", stats);
            model.addAttribute("activePage", "dashboard");
            
            // Add individual stats as separate attributes for debugging
            model.addAttribute("totalUsers", stats.get("totalUsers"));
            model.addAttribute("totalProducts", stats.get("totalProducts"));
            model.addAttribute("totalOrders", stats.get("totalOrders"));
            model.addAttribute("totalRevenue", stats.get("totalRevenue"));
            model.addAttribute("pendingOrders", stats.get("pendingOrders"));
            
            return "admin/dashboard";
        } catch (Exception e) {
            // Log the error
            System.err.println("Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Add error message to model
            model.addAttribute("errorMessage", "Error loading dashboard data: " + e.getMessage());
            
            // Add empty stats to prevent null pointer
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalUsers", 0L);
            emptyStats.put("totalProducts", 0L);
            emptyStats.put("totalOrders", 0L);
            emptyStats.put("totalRevenue", BigDecimal.ZERO);
            emptyStats.put("pendingOrders", 0L);
            model.addAttribute("stats", emptyStats);
            
            return "admin/dashboard";
        }
    }
}
