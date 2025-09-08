package com.AuroraDecors.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.AuroraDecors.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    /**
     * Find the user with the highest ID that is less than the given ID
     */
    Optional<User> findTopByIdLessThanOrderByIdDesc(Long id);
    
    /**
     * Find the user with the lowest ID that is greater than the given ID
     */
    Optional<User> findTopByIdGreaterThanOrderByIdAsc(Long id);
    
    /**
     * Count users with ID less than or equal to the given ID
     */
    long countByIdLessThanEqual(Long id);
}
