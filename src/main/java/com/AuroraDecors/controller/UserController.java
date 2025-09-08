package com.AuroraDecors.controller;

import com.AuroraDecors.dto.UserResponse;
import com.AuroraDecors.entity.User;
import com.AuroraDecors.service.CloudinaryService;
import com.AuroraDecors.service.UserService;
import com.AuroraDecors.util.PasswordUtil;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        System.out.println("=== PROFILE DEBUG ===");
        System.out.println("User ID from session: " + userId);
        
        if (userId == null) {
            System.out.println("No user ID in session - showing guest view");
            model.addAttribute("guest", true);
            return "profile";
        }
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("User found: " + user.getUsername());
            model.addAttribute("user", user);
            
            return "profile";
        }
        System.out.println("User not found for ID: " + userId);
        model.addAttribute("guest", true);
        return "profile";
    }

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!PasswordUtil.verifyPassword(currentPassword, user.getPassword())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
                    return "redirect:/profile";
                }
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
                    return "redirect:/profile";
                }
                user.setPassword(PasswordUtil.hashPassword(newPassword));
                userService.saveUser(user);
                redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }



    @PostMapping("/delete")
    public String deleteAccount(HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        try {
            userService.deleteUser(userId);
            session.invalidate();
            return "redirect:/auth/login?deleted";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile";
        }
    }
    
    @PostMapping("/update")
    public String updateProfile(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam(required = false) String profileImageUrl,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        
        try {
            userService.updateProfile(userId, username, email, profileImageUrl);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            
            // Refresh session user for navbar and other fragments
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                session.setAttribute("username", userOpt.get().getUsername());
                session.setAttribute("loggedInUser", new UserResponse(userOpt.get()));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/upload-image")
    public String uploadProfileImage(@RequestParam("image") MultipartFile file,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";
        
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select an image to upload.");
                return "redirect:/profile";
            }
            
            // Upload image to Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file, "profile-images");
            
            // Update user's profile image
            userService.updateProfileImage(userId, imageUrl);
            
            // Refresh session user with new image so navbar/profile use it instantly
            Optional<User> userOpt = userService.findById(userId);
            userOpt.ifPresent(value -> session.setAttribute("loggedInUser", new UserResponse(value)));
            
            redirectAttributes.addFlashAttribute("successMessage", "Profile image updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading image: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
} 