package com.tabonfurniture.controller;

import com.tabonfurniture.entity.Category;
import com.tabonfurniture.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/debug-categories")
    @ResponseBody
    public String debugCategories() {
        List<Category> categories = categoryService.getAllCategories();
        StringBuilder result = new StringBuilder();
        result.append("Categories in database:\n");
        for (Category category : categories) {
            result.append("ID: ").append(category.getId())
                  .append(", Name: ").append(category.getName())
                  .append(", Description: ").append(category.getDescription())
                  .append(", Active: ").append(category.getIsActive())
                  .append("\n");
        }
        return result.toString();
    }
    
    @GetMapping("/test-session")
    @ResponseBody
    public String testSession(HttpSession session) {
        System.out.println("Debug - testSession called");
        System.out.println("Debug - Session ID: " + session.getId());
        System.out.println("Debug - Session is new: " + session.isNew());
        
        // Set a test attribute
        session.setAttribute("testAttribute", "testValue");
        
        return "Session test - ID: " + session.getId() + ", isNew: " + session.isNew();
    }
    
    @GetMapping
    public String listCategories(Model model, HttpSession session) {
        System.out.println("Debug - listCategories method called");
        System.out.println("Debug - Session ID: " + session.getId());
        System.out.println("Debug - Session is new: " + session.isNew());
        System.out.println("Debug - Session creation time: " + session.getCreationTime());
        System.out.println("Debug - Session last accessed time: " + session.getLastAccessedTime());
        
        if (!isAdmin(session)) {
            System.out.println("Debug - listCategories: Admin check failed, redirecting to login");
            return "redirect:/auth/login";
        }

        System.out.println("Debug - listCategories: Admin check passed, loading categories");
        List<Category> categories = categoryService.getAllCategories();
        
        // Debug: Check category IDs
        System.out.println("Debug - Categories loaded:");
        for (Category category : categories) {
            System.out.println("  Category: " + category.getName() + ", ID: " + category.getId());
        }
        
        categoryService.populateProductCounts(categories);
        model.addAttribute("categories", categories);
        model.addAttribute("activePage", "categories");
        
        return "admin/categories/list";
    }

    @GetMapping("/create")
    public String showCreateCategoryForm(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }

        model.addAttribute("category", new Category());
        model.addAttribute("activePage", "categories");
        return "admin/categories/create";
    }

    @PostMapping("/create")
    public String createCategory(@ModelAttribute Category category, 
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }

        try {
            System.out.println("Debug - Creating category: " + category.getName());
            System.out.println("Debug - Category before save - ID: " + category.getId());
            
            // Check if category name already exists
            if (categoryService.categoryExists(category.getName())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Category with this name already exists!");
                return "redirect:/admin/categories/create";
            }

            Category savedCategory = categoryService.saveCategory(category);
            System.out.println("Debug - Category after save - ID: " + savedCategory.getId());
            System.out.println("Debug - Category after save - Name: " + savedCategory.getName());
            
            redirectAttributes.addFlashAttribute("successMessage", "Category created successfully!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            System.out.println("Debug - Error creating category: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating category: " + e.getMessage());
            return "redirect:/admin/categories/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditCategoryForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        System.out.println("Debug - Edit Category: Checking admin access for category ID: " + id);
        System.out.println("Debug - Session ID: " + session.getId());
        System.out.println("Debug - Session is new: " + session.isNew());
        System.out.println("Debug - Session creation time: " + session.getCreationTime());
        System.out.println("Debug - Session last accessed time: " + session.getLastAccessedTime());
        System.out.println("Debug - Session userRole: " + session.getAttribute("userRole"));
        System.out.println("Debug - Session loggedInUser: " + session.getAttribute("loggedInUser"));
        System.out.println("Debug - Session userId: " + session.getAttribute("userId"));
        System.out.println("Debug - Session username: " + session.getAttribute("username"));
        
        if (!isAdmin(session)) {
            System.out.println("Debug - Edit Category: Admin check failed, redirecting to login");
            return "redirect:/auth/login";
        }

        System.out.println("Debug - Edit Category: Admin check passed, loading category");
        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            System.out.println("Debug - Edit Category: Found category - ID: " + category.getId() + ", Name: " + category.getName());
            model.addAttribute("category", category);
            model.addAttribute("activePage", "categories");
            return "admin/categories/edit";
        }
        
        System.out.println("Debug - Edit Category: Category not found, redirecting to list");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/edit")
    public String updateCategory(@PathVariable("id") Long id,
                               @ModelAttribute Category category,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }

        try {
            Optional<Category> existingCategoryOpt = categoryService.getCategoryById(id);
            if (existingCategoryOpt.isPresent()) {
                Category existingCategory = existingCategoryOpt.get();
                
                // Check if name is being changed and if it already exists
                if (!existingCategory.getName().equals(category.getName()) && 
                    categoryService.categoryExists(category.getName())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Category with this name already exists!");
                    return "redirect:/admin/categories/" + id + "/edit";
                }
                
                category.setId(id);
                categoryService.saveCategory(category);
                redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
            }
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating category: " + e.getMessage());
            return "redirect:/admin/categories/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        System.out.println("Debug - Delete Category: Checking admin access for category ID: " + id);
        System.out.println("Debug - Session userRole: " + session.getAttribute("userRole"));
        System.out.println("Debug - Session loggedInUser: " + session.getAttribute("loggedInUser"));
        
        if (!isAdmin(session)) {
            System.out.println("Debug - Delete Category: Admin check failed, redirecting to login");
            return "redirect:/auth/login";
        }

        try {
            Optional<Category> categoryOpt = categoryService.getCategoryById(id);
            if (categoryOpt.isPresent()) {
                Category category = categoryOpt.get();
                
                // Check if category has products
                if (category.getProductCount() > 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Cannot delete category '" + category.getName() + "' because it has " + 
                        category.getProductCount() + " product(s). Please remove or reassign the products first.");
                } else {
                    categoryService.deleteCategory(id);
                    redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting category: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
    
    private boolean isAdmin(HttpSession session) {
        System.out.println("Debug - isAdmin method called");
        System.out.println("Debug - Session ID: " + session.getId());
        System.out.println("Debug - Session is new: " + session.isNew());
        
        Object userRole = session.getAttribute("userRole");
        Object loggedInUser = session.getAttribute("loggedInUser");
        Object userId = session.getAttribute("userId");
        Object username = session.getAttribute("username");
        
        System.out.println("Debug - Session attributes:");
        System.out.println("  userRole: " + userRole);
        System.out.println("  loggedInUser: " + loggedInUser);
        System.out.println("  userId: " + userId);
        System.out.println("  username: " + username);
        
        boolean isAdmin = userRole != null && userRole.toString().equals("ADMIN");
        System.out.println("Debug - isAdmin result: " + isAdmin);
        return isAdmin;
    }
} 