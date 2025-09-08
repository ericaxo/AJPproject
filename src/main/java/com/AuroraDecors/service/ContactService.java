package com.AuroraDecors.service;

import com.AuroraDecors.entity.Contact;
import com.AuroraDecors.entity.User;
import com.AuroraDecors.repository.ContactRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ContactService {
    
    @Autowired
    private ContactRepository contactRepository;
    
    public Contact saveContact(Contact contact) {
        return contactRepository.save(contact);
    }
    
    public Contact saveContactWithUser(Contact contact, User user) {
        contact.setUser(user);
        return contactRepository.save(contact);
    }
    
    public List<Contact> getAllContacts() {
        return contactRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public List<Contact> getContactsByUser(User user) {
        return contactRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Contact> getContactsByStatus(Contact.Status status) {
        return contactRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    public Optional<Contact> getContactById(Long id) {
        return contactRepository.findById(id);
    }
    
    @Transactional
    public Contact updateContactStatus(Long id, Contact.Status status) {
        Optional<Contact> contactOpt = contactRepository.findById(id);
        if (contactOpt.isPresent()) {
            Contact contact = contactOpt.get();
            contact.setStatus(status);
            contact.setUpdatedAt(LocalDateTime.now());
            return contactRepository.save(contact);
        }
        return null;
    }
    
    @Transactional
    public Contact addAdminFeedback(Long id, String feedback) {
        Optional<Contact> contactOpt = contactRepository.findById(id);
        if (contactOpt.isPresent()) {
            Contact contact = contactOpt.get();
            contact.setAdminFeedback(feedback);
            contact.setFeedbackDate(LocalDateTime.now());
            contact.setUpdatedAt(LocalDateTime.now());
            return contactRepository.save(contact);
        }
        return null;
    }
    
    public long getContactCountByStatus(Contact.Status status) {
        return contactRepository.countByStatus(status);
    }
    
    public long getTotalContacts() {
        return contactRepository.count();
    }
    
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }
}

















