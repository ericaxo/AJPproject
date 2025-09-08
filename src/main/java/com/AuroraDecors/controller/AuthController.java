package com.AuroraDecors.controller;

import com.AuroraDecors.dto.LoginRequest;
import com.AuroraDecors.dto.PasswordResetRequest;
import com.AuroraDecors.dto.SignupRequest;
import com.AuroraDecors.dto.UserResponse;
import com.AuroraDecors.entity.User;
import com.AuroraDecors.service.EmailService;
import com.AuroraDecors.service.UserService;

import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/signup")
    public String processSignup(@Valid @ModelAttribute SignupRequest signupRequest,
                                BindingResult bindingResult,
                                Model model) {

        model.addAttribute("loginRequest", new LoginRequest());

        // Show signup form on validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "signup");
            return "auth/login";
        }

        try {
            UserResponse user = userService.signup(signupRequest);
            model.addAttribute("successMessage", "Account created successfully! Please login.");
            model.addAttribute("activeTab", "login");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("activeTab", "signup");
        }

        return "auth/login";
    }
    
    // Show login form
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        model.addAttribute("signupRequest", new SignupRequest());
        return "auth/login";
    }
    
    // Show signup form
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        model.addAttribute("signupRequest", new SignupRequest());
        model.addAttribute("activeTab", "signup");
        return "auth/login";
    }
    
    // Process login
    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute LoginRequest loginRequest,
                              BindingResult bindingResult,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        model.addAttribute("signupRequest", new SignupRequest());
        
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }
        
        try {
            UserResponse user = userService.login(loginRequest);
            
            // Store user in session
            session.setAttribute("loggedInUser", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userRole", user.getRole());
            
            // Send login notification email (best-effort)
            try {
                if (user.getEmail() != null && !user.getEmail().isBlank()) {
                    emailService.sendLoginNotification(user.getEmail(), user.getUsername());
                }
            } catch (Exception ignore) {
                // Do not block login on email failures
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Welcome back, " + user.getUsername() + "!");
            
            // Redirect based on role
            if (user.getRole() == com.AuroraDecors.entity.User.Role.ADMIN) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/products";
            }
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
        }
    }
    
    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
        return "redirect:/";
    }
    
    // Show forgot password form
    @GetMapping("/forgot")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("passwordResetRequest", new PasswordResetRequest());
        return "auth/forgot";
    }
    
    // Send OTP for password reset
    @PostMapping("/forgot/send-otp")
    @ResponseBody
    public String sendOTP(@Valid @ModelAttribute PasswordResetRequest request, 
                         BindingResult bindingResult,
                         HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "error";
        }
        
        try {
            // Check if user exists with this email
            Optional<User> userOptional = userService.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                return "email_not_found";
            }
            
            // Generate 6-digit OTP
            String otp = String.format("%06d", (int)(Math.random() * 1000000));
            
            System.out.println("Debug - Generated OTP: " + otp + " for email: " + request.getEmail());
            
            // Store OTP in session
            session.setAttribute("otp_" + request.getEmail(), otp);
            session.setAttribute("otp_timestamp_" + request.getEmail(), System.currentTimeMillis());
            
            // Send OTP via email
            emailService.sendOTP(request.getEmail(), otp);
            
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
    
    // Verify OTP only
    @PostMapping("/forgot/verify-otp")
    @ResponseBody
    public String verifyOTP(@Valid @ModelAttribute PasswordResetRequest request,
                           BindingResult bindingResult,
                           HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "error";
        }
        
        try {
            // Verify OTP from session
            String storedOtp = (String) session.getAttribute("otp_" + request.getEmail());
            Long timestamp = (Long) session.getAttribute("otp_timestamp_" + request.getEmail());
            
            System.out.println("Debug - Email: " + request.getEmail());
            System.out.println("Debug - Entered OTP: " + request.getOtp());
            System.out.println("Debug - Stored OTP: " + storedOtp);
            System.out.println("Debug - Timestamp: " + timestamp);
            
            if (storedOtp == null || timestamp == null) {
                System.out.println("Debug - OTP or timestamp is null");
                return "error";
            }
            
            // Check if OTP is expired (10 minutes)
            long currentTime = System.currentTimeMillis();
            if (currentTime - timestamp > 10 * 60 * 1000) { // 10 minutes
                System.out.println("Debug - OTP is expired");
                // Clear expired OTP
                session.removeAttribute("otp_" + request.getEmail());
                session.removeAttribute("otp_timestamp_" + request.getEmail());
                return "error";
            }
            
            // Verify OTP
            if (!storedOtp.equals(request.getOtp())) {
                System.out.println("Debug - OTP mismatch. Stored: " + storedOtp + ", Entered: " + request.getOtp());
                return "error";
            }
            
            System.out.println("Debug - OTP verification successful");
            
            // Mark OTP as verified in session
            session.setAttribute("otp_verified_" + request.getEmail(), true);
            
            return "success";
        } catch (Exception e) {
            System.out.println("Debug - Exception: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }
    
    // Reset password after OTP verification
    @PostMapping("/forgot/reset-password")
    @ResponseBody
    public String resetPassword(@Valid @ModelAttribute PasswordResetRequest request,
                              BindingResult bindingResult,
                              HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "error";
        }
        
        try {
            // Check if OTP was verified
            Boolean otpVerified = (Boolean) session.getAttribute("otp_verified_" + request.getEmail());
            if (otpVerified == null || !otpVerified) {
                System.out.println("Debug - OTP not verified");
                return "error";
            }
            
            // Check if new password is provided
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                System.out.println("Debug - New password is null or empty");
                return "error";
            }
            
            System.out.println("Debug - Resetting password for email: " + request.getEmail());
            
            // Update password in database
            userService.resetPassword(request.getEmail(), request.getNewPassword());
            
            // Clear OTP verification from session
            session.removeAttribute("otp_verified_" + request.getEmail());
            session.removeAttribute("otp_" + request.getEmail());
            session.removeAttribute("otp_timestamp_" + request.getEmail());
            
            // Send confirmation email
            emailService.sendPasswordResetSuccess(request.getEmail(), "User");
            
            return "success";
        } catch (Exception e) {
            System.out.println("Debug - Exception: " + e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }
    
    // Get current user info for JavaScript
    @GetMapping("/current-user")
    @ResponseBody
    public Map<String, Object> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Object userObj = session.getAttribute("loggedInUser");
        if (userObj != null && userObj instanceof UserResponse userResponse) {
            response.put("success", true);
            response.put("user", userResponse);
        } else {
            response.put("success", false);
            response.put("message", "No user logged in");
        }
        
        return response;
    }
}
