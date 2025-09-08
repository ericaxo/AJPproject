package com.AuroraDecors.controller;

import com.AuroraDecors.entity.Contact;
import com.AuroraDecors.service.ContactService;
import com.AuroraDecors.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactController {
    
    @Autowired
    private ContactService contactService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/my-contacts")
    public String myContacts(Model model, HttpSession session) {
        Object userResponse = session.getAttribute("loggedInUser");
        if (userResponse == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("user", userResponse);
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            userService.findById(userId).ifPresent(user -> {
                List<Contact> contacts = contactService.getContactsByUser(user);
                model.addAttribute("contacts", contacts);
                model.addAttribute("totalContacts", contacts.size());
            });
        }
        
        return "contacts/my-contacts";
    }
}










