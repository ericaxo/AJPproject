package com.tabonfurniture.controller;

import com.tabonfurniture.entity.Product;
import com.tabonfurniture.service.ProductService;
import com.tabonfurniture.service.CloudinaryService;
import com.tabonfurniture.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping
    public String listProducts(Model model,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              @RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "category", required = false) String category,
                              HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> productPage;
        
        if (search != null && !search.trim().isEmpty()) {
            productPage = productService.searchProducts(search, pageable);
        } else if (category != null && !category.trim().isEmpty()) {
            productPage = productService.getProductsByCategory(category, pageable);
        } else {
            productPage = productService.getAllProducts(pageable);
        }
        
        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("activePage", "products");
        
        return "admin/products/products";
    }
    
    @GetMapping("/create")
    public String showCreateProductForm(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "admin/products/create";
    }
    
    @PostMapping("/create")
    public String createProduct(@ModelAttribute Product product,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            // Validate required fields
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Product name is required");
                return "redirect:/admin/products/create";
            }
            
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Product price is required and must be greater than 0");
                return "redirect:/admin/products/create";
            }
            
            if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Product stock quantity is required and must be 0 or greater");
                return "redirect:/admin/products/create";
            }
            
            
            
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                // Upload image to Cloudinary
                String imageUrl = cloudinaryService.uploadImage(imageFile, "tabonfurniture/products");
                product.setImageUrl(imageUrl);
                System.out.println("Image uploaded successfully to Cloudinary: " + product.getImageUrl());
            } else {
                // Set a default image URL if no image is uploaded
                product.setImageUrl("https://images.unsplash.com/photo-1493666438817-866a91353ca9?w=400&h=400&fit=crop");
                System.out.println("Setting default image URL for new product");
            }
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Product created successfully: " + product.getName());
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error creating product: " + e.getMessage());
            return "redirect:/admin/products/create";
        }
    }
    
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable("id") Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            return "admin/products/view";
        }
        
        return "redirect:/admin/products";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditProductForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("activePage", "products");
            return "admin/products/edit";
        }
        
        return "redirect:/admin/products";
    }
    
    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable("id") Long id,
                               @ModelAttribute Product product,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        System.out.println("=== UPDATE PRODUCT REQUEST ===");
        System.out.println("Product ID: " + id);
        System.out.println("Product name: " + product.getName());
        System.out.println("Product price: " + product.getPrice());
        System.out.println("Product category: " + product.getCategory());
        
        System.out.println("Product description: " + product.getDescription());
        System.out.println("Product dimensions: " + product.getDimensions());
        System.out.println("Product color: " + product.getColor());
        System.out.println("Product material: " + product.getMaterial());
        
        System.out.println("Product stockQuantity: " + product.getStockQuantity());
        
        if (!isAdmin(session)) {
            System.out.println("User is not admin, redirecting to login");
            return "redirect:/auth/login";
        }
        
        try {
            // Validate required fields
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                System.out.println("Product name is required");
                redirectAttributes.addFlashAttribute("errorMessage", "Product name is required");
                return "redirect:/admin/products/" + id + "/edit";
            }
            
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Product price is required and must be greater than 0");
                redirectAttributes.addFlashAttribute("errorMessage", "Product price is required and must be greater than 0");
                return "redirect:/admin/products/" + id + "/edit";
            }
            
            if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
                System.out.println("Product stock quantity is required and must be 0 or greater");
                redirectAttributes.addFlashAttribute("errorMessage", "Product stock quantity is required and must be 0 or greater");
                return "redirect:/admin/products/" + id + "/edit";
            }
            
            
            
            // Get existing product to preserve image URL if no new image is uploaded
            Optional<Product> existingProductOpt = productService.getProductById(id);
            if (existingProductOpt.isPresent()) {
                Product existingProduct = existingProductOpt.get();
                // Preserve existing image URL if no new image is uploaded
                if (imageFile == null || imageFile.isEmpty()) {
                    String existingImageUrl = existingProduct.getImageUrl();
                    if (existingImageUrl != null && !existingImageUrl.isEmpty() && !existingImageUrl.equals("null")) {
                        product.setImageUrl(existingImageUrl);
                        System.out.println("Preserving existing image URL: " + existingImageUrl);
                    } else {
                        // Set a default image URL if none exists
                        product.setImageUrl("https://images.unsplash.com/photo-1493666438817-866a91353ca9?w=400&h=400&fit=crop");
                        System.out.println("Setting default image URL for product without image");
                    }
                }
            }
            
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    // Upload image to Cloudinary
                    String imageUrl = cloudinaryService.uploadImage(imageFile, "tabonfurniture/products");
                    product.setImageUrl(imageUrl);
                    System.out.println("Image uploaded successfully to Cloudinary: " + product.getImageUrl());
                } catch (Exception e) {
                    System.err.println("Error uploading image to Cloudinary: " + e.getMessage());
                    redirectAttributes.addFlashAttribute("errorMessage", "Error uploading image: " + e.getMessage());
                    return "redirect:/admin/products/" + id + "/edit";
                }
            }
            
            product.setId(id);
            System.out.println("Final product image URL: " + product.getImageUrl());
            Product savedProduct = productService.saveProduct(product);
            System.out.println("Product saved successfully with ID: " + savedProduct.getId());
            System.out.println("Saved product image URL: " + savedProduct.getImageUrl());
            
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            return "redirect:/admin/products/" + id;
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating product: " + e.getMessage());
            return "redirect:/admin/products/" + id + "/edit";
        }
    }
    
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            // Get the product to delete its image from Cloudinary
            Optional<Product> productOpt = productService.getProductById(id);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                String imageUrl = product.getImageUrl();
                
                // Delete image from Cloudinary if it's a Cloudinary URL
                if (imageUrl != null && imageUrl.contains("cloudinary.com")) {
                    try {
                        String publicId = cloudinaryService.getPublicIdFromUrl(imageUrl);
                        if (publicId != null) {
                            cloudinaryService.deleteImage(publicId);
                            System.out.println("Image deleted from Cloudinary: " + publicId);
                        }
                    } catch (Exception e) {
                        System.err.println("Error deleting image from Cloudinary: " + e.getMessage());
                        // Continue with product deletion even if image deletion fails
                    }
                }
            }
            
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting product: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    private boolean isAdmin(HttpSession session) {
        Object userRole = session.getAttribute("userRole");
        return userRole != null && userRole.toString().equals("ADMIN");
    }
}
