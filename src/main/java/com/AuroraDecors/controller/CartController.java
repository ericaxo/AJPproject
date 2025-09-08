package com.AuroraDecors.controller;

import com.AuroraDecors.entity.Cart;
import com.AuroraDecors.entity.Product;
import com.AuroraDecors.entity.User;
import com.AuroraDecors.service.CartService;
import com.AuroraDecors.service.ProductService;
import com.AuroraDecors.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String showCart(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        Cart cart = cartService.getCartByUserId(userId);
        model.addAttribute("cart", cart);
        return "cart/view";
    }
    
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                           @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        try {
            Optional<User> userOpt = userService.findById(userId);
            Optional<Product> productOpt = productService.getProductById(productId);
            
            if (userOpt.isPresent() && productOpt.isPresent()) {
                cartService.addToCart(userOpt.get(), productOpt.get(), quantity);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Product added to cart successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Product not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error adding product to cart: " + e.getMessage());
        }
        
        return "redirect:/products/" + productId;
    }

    // Provide a JSON response on the same endpoint when explicitly requested via ajax=true
    @PostMapping(value = "/add", params = "ajax=true")
    @ResponseBody
    public CartAddResponse addToCartJson(@RequestParam Long productId,
                                         @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                                         HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new CartAddResponse(false, "Please login to add items to cart", "/auth/login");
        }

        try {
            Optional<User> userOpt = userService.findById(userId);
            Optional<Product> productOpt = productService.getProductById(productId);

            if (userOpt.isPresent() && productOpt.isPresent()) {
                cartService.addToCart(userOpt.get(), productOpt.get(), quantity);
                return new CartAddResponse(true, "Product added to cart successfully!", "/cart");
            } else {
                return new CartAddResponse(false, "Product not found!", null);
            }
        } catch (Exception e) {
            return new CartAddResponse(false, "Error adding product to cart: " + e.getMessage(), null);
        }
    }
    
    @PostMapping("/add-ajax")
    @ResponseBody
    public CartAddResponse addToCartAjax(@RequestParam Long productId,
                                        @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                                        HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new CartAddResponse(false, "Please login to add items to cart", "/auth/login");
        }
        
        try {
            Optional<User> userOpt = userService.findById(userId);
            Optional<Product> productOpt = productService.getProductById(productId);
            
            if (userOpt.isPresent() && productOpt.isPresent()) {
                cartService.addToCart(userOpt.get(), productOpt.get(), quantity);
                return new CartAddResponse(true, "Product added to cart successfully!", "/cart");
            } else {
                return new CartAddResponse(false, "Product not found!", null);
            }
        } catch (Exception e) {
            return new CartAddResponse(false, "Error adding product to cart: " + e.getMessage(), null);
        }
    }
    
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                cartService.removeFromCart(userOpt.get(), productId);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Product removed from cart!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error removing product from cart: " + e.getMessage());
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/update")
    public String updateCartItem(@RequestParam Long productId,
                                @RequestParam(value = "quantity") Integer quantity,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                cartService.updateCartItemQuantity(userOpt.get(), productId, quantity);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Cart updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating cart: " + e.getMessage());
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                cartService.clearCart(userOpt.get());
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Cart cleared successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error clearing cart: " + e.getMessage());
        }
        
        return "redirect:/cart";
    }
    
    @GetMapping("/count")
    @ResponseBody
    public CartCountResponse getCartCount(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return new CartCountResponse(0);
        }
        
        try {
            Cart cart = cartService.getCartByUserId(userId);
            int count = cart != null ? cart.getCartItems().size() : 0;
            return new CartCountResponse(count);
        } catch (Exception e) {
            return new CartCountResponse(0);
        }
    }
    
    // Response class for cart count
    public static class CartCountResponse {
        private int count;
        
        public CartCountResponse(int count) {
            this.count = count;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
    }
    
    // Response class for cart add AJAX
    public static class CartAddResponse {
        private boolean success;
        private String message;
        private String redirectUrl;
        
        public CartAddResponse(boolean success, String message, String redirectUrl) {
            this.success = success;
            this.message = message;
            this.redirectUrl = redirectUrl;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getRedirectUrl() {
            return redirectUrl;
        }
        
        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
    }
}
