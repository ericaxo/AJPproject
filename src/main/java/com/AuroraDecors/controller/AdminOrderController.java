package com.AuroraDecors.controller;

import com.AuroraDecors.entity.Order;
import com.AuroraDecors.service.OrderService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    public String listOrders(Model model,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            HttpSession session) {
        
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("activePage", "orders");
        
        return "admin/orders/list";
    }
    
    @GetMapping("/pending")
    public String listPendingOrders(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        List<Order> pendingOrders = orderService.getOrdersByStatus(Order.OrderStatus.PENDING);
        model.addAttribute("orders", pendingOrders);
        model.addAttribute("title", "Pending Orders");
        model.addAttribute("activePage", "orders");
        
        return "admin/orders/list";
    }
    
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable("id") Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isPresent()) {
            model.addAttribute("order", orderOpt.get());
            model.addAttribute("activePage", "orders");
            return "admin/orders/view";
        }
        
        return "redirect:/admin/orders";
    }
    
    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable("id") Long id,
                                   @RequestParam Order.OrderStatus status,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating order status: " + e.getMessage());
        }
        
        return "redirect:/admin/orders/" + id;
    }
    
    @PostMapping("/{id}/update-payment")
    public String updatePaymentStatus(@PathVariable("id") Long id,
                                     @RequestParam Order.PaymentStatus paymentStatus,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            orderService.updatePaymentStatus(id, paymentStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Payment status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating payment status: " + e.getMessage());
        }
        
        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/create")
    public String showCreateOrderForm(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        model.addAttribute("order", new Order());
        return "admin/orders/create";
    }

    @PostMapping("/create")
    public String createOrder(@ModelAttribute Order order, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        orderService.saveOrder(order);
        redirectAttributes.addFlashAttribute("successMessage", "Order created successfully!");
        return "redirect:/admin/orders";
    }

    @GetMapping("/{id}/edit")
    public String showEditOrderForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isPresent()) {
            model.addAttribute("order", orderOpt.get());
            return "admin/orders/edit";
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/{id}/edit")
    public String updateOrder(@PathVariable("id") Long id, @ModelAttribute Order order, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        order.setId(id);
        orderService.saveOrder(order);
        redirectAttributes.addFlashAttribute("successMessage", "Order updated successfully!");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteOrder(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        orderService.deleteOrder(id);
        redirectAttributes.addFlashAttribute("successMessage", "Order deleted successfully!");
        return "redirect:/admin/orders";
    }
    
    private boolean isAdmin(HttpSession session) {
        Object userRole = session.getAttribute("userRole");
        return userRole != null && userRole.toString().equals("ADMIN");
    }
}
