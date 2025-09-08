package com.AuroraDecors.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.AuroraDecors.entity.Contact;
import com.AuroraDecors.entity.User;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    List<Contact> findByUserOrderByCreatedAtDesc(User user);
    
    List<Contact> findByStatusOrderByCreatedAtDesc(Contact.Status status);
    
    List<Contact> findAllByOrderByCreatedAtDesc();
    
    long countByStatus(Contact.Status status);
    
    // Delete contacts by user ID
    void deleteByUserId(Long userId);
}

