package com.tabonfurniture.controller;

import com.tabonfurniture.dto.SignupRequest;
import com.tabonfurniture.dto.UserResponse;
import com.tabonfurniture.entity.User;
import com.tabonfurniture.service.UserService;
import com.tabonfurniture.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    public String listUsers(Model model, 
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size,
                           HttpSession session) {
        
        // Check admin access
        if (!isAdmin(session)) {
            // If not admin but logged in, redirect to home with message rather than exposing admin content
            return "redirect:/auth/login";
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userService.getAllUsers(pageable);
        
        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("activePage", "users");
        
        return "admin/users/list";
    }
    
    @GetMapping("/create")
    public String showCreateUserForm(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("signupRequest", new SignupRequest());
        return "admin/users/create";
    }
    
    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute SignupRequest signupRequest,
                            BindingResult bindingResult,
                            Model model,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        if (bindingResult.hasErrors()) {
            return "admin/users/create";
        }
        
        try {
            UserResponse user = userService.signup(signupRequest);
            redirectAttributes.addFlashAttribute("successMessage", 
                "User created successfully: " + user.getUsername());
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/users/create";
        }
    }
    
    @GetMapping("/{id}")
    public String viewUser(@PathVariable("id") Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes, HttpServletResponse response) {
        // Add aggressive cache-busting headers
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setHeader("Surrogate-Control", "no-store");
        response.setHeader("Vary", "*");
        response.setDateHeader("Last-Modified", System.currentTimeMillis());
        response.setDateHeader("Date", System.currentTimeMillis());
        response.setHeader("ETag", "\"" + System.currentTimeMillis() + "\"");
        
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        System.out.println("🔍 Looking for user with ID: " + id);
        
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("✅ Found user: " + user.getUsername() + " (ID: " + user.getId() + ")");
            System.out.println("📊 User details - Username: " + user.getUsername() + ", Email: " + user.getEmail() + ", Role: " + user.getRole());
            System.out.println("🔍 Model will contain user ID: " + user.getId());
            
            // Calculate user statistics
            int totalOrders = orderService.getOrderCountByUserId(user.getId());
            double totalSpent = orderService.getTotalSpentByUserId(user.getId());
            
            // Get previous and next user IDs for navigation
            Long previousUserId = userService.getPreviousUserId(user.getId());
            Long nextUserId = userService.getNextUserId(user.getId());
            
            // Get user position and total count
            int userPosition = userService.getUserPosition(user.getId());
            long totalUsers = userService.getTotalUserCount();
            
            System.out.println("🧭 Navigation data - Previous: " + previousUserId + ", Next: " + nextUserId + ", Position: " + userPosition + "/" + totalUsers);
            
            model.addAttribute("viewUser", user);
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalSpent", totalSpent);
            model.addAttribute("lastLogin", "Never"); // User entity doesn't have lastLoginAt field
            model.addAttribute("activePage", "users");
            model.addAttribute("previousUserId", previousUserId);
            model.addAttribute("nextUserId", nextUserId);
            model.addAttribute("userPosition", userPosition);
            model.addAttribute("totalUsers", totalUsers);
            return "admin/users/view";
        }
        System.out.println("❌ User with ID " + id + " not found");
        redirectAttributes.addFlashAttribute("errorMessage", "User with ID " + id + " not found");
        return "redirect:/admin/users";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditUserForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            model.addAttribute("activePage", "users");
            return "admin/users/edit";
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable("id") Long id,
                            @RequestParam(value = "username") String username,
                            @RequestParam(value = "email") String email,
                            @RequestParam User.Role role,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            userService.updateUser(id, username, email, role);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/admin/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        System.out.println("🗑️ Attempting to delete user with ID: " + id);
        
        try {
            // Check if user exists first
            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("✅ Found user to delete: " + user.getUsername() + " (ID: " + user.getId() + ", Role: " + user.getRole() + ")");
                
                // Prevent deleting admin users
                if (user.getRole() == User.Role.ADMIN) {
                    System.out.println("❌ Cannot delete admin user: " + user.getUsername());
                    redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete admin users");
                    return "redirect:/admin/users";
                }
                
                userService.deleteUser(id);
                System.out.println("✅ User deleted successfully: " + user.getUsername());
                redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
            } else {
                System.out.println("❌ User with ID " + id + " not found");
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            }
        } catch (Exception e) {
            System.out.println("❌ Error deleting user: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    

    
    @GetMapping("/test/users")
    @ResponseBody
    public String testUsers() {
        StringBuilder result = new StringBuilder();
        result.append("Available Users:\n");
        userService.getAllUsers(PageRequest.of(0, 100)).getContent().forEach(user -> {
            result.append("ID: ").append(user.getId()).append(", Username: ").append(user.getUsername()).append(", Email: ").append(user.getEmail()).append("\n");
        });
        return result.toString();
    }
    
    private boolean isAdmin(HttpSession session) {
        Object userRole = session.getAttribute("userRole");
        return userRole != null && userRole.toString().equals("ADMIN");
    }
}
