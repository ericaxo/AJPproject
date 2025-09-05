package com.tabonfurniture.controller;

import com.tabonfurniture.entity.Contact;
import com.tabonfurniture.service.ContactService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/contacts")
public class AdminContactController {
    
    @Autowired
    private ContactService contactService;
    
    private boolean isAdmin(HttpSession session) {
        Object userRole = session.getAttribute("userRole");
        return userRole != null && userRole.toString().equals("ADMIN");
    }
    
    @GetMapping
    public String listContacts(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        List<Contact> contacts = contactService.getAllContacts();
        
        // Calculate counts by status
        long pendingCount = contactService.getContactCountByStatus(Contact.Status.PENDING);
        long inProgressCount = contactService.getContactCountByStatus(Contact.Status.IN_PROGRESS);
        long resolvedCount = contactService.getContactCountByStatus(Contact.Status.RESOLVED);
        long closedCount = contactService.getContactCountByStatus(Contact.Status.CLOSED);
        
        model.addAttribute("contacts", contacts);
        model.addAttribute("totalContacts", contacts.size());
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("closedCount", closedCount);
        
        return "admin/contacts/list";
    }
    
    @GetMapping("/{id}")
    public String viewContact(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        return contactService.getContactById(id)
            .map(contact -> {
                model.addAttribute("contact", contact);
                return "admin/contacts/view";
            })
            .orElse("redirect:/admin/contacts");
    }
    
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                              @RequestParam String status,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            Contact.Status newStatus = Contact.Status.valueOf(status);
            contactService.updateContactStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Contact status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + e.getMessage());
        }
        
        return "redirect:/admin/contacts/" + id;
    }
    
    @PostMapping("/{id}/feedback")
    public String addFeedback(@PathVariable Long id,
                             @RequestParam String feedback,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            contactService.addAdminFeedback(id, feedback);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding feedback: " + e.getMessage());
        }
        
        return "redirect:/admin/contacts/" + id;
    }
    
    @PostMapping("/{id}/delete")
    public String deleteContact(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/auth/login";
        }
        
        try {
            contactService.deleteContact(id);
            redirectAttributes.addFlashAttribute("successMessage", "Contact deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting contact: " + e.getMessage());
        }
        
        return "redirect:/admin/contacts";
    }
}
