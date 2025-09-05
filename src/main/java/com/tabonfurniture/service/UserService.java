package com.tabonfurniture.service;

import com.tabonfurniture.dto.LoginRequest;
import com.tabonfurniture.dto.SignupRequest;
import com.tabonfurniture.dto.UserResponse;
import com.tabonfurniture.entity.User;
import com.tabonfurniture.repository.UserRepository;
import com.tabonfurniture.repository.ContactRepository;
import com.tabonfurniture.repository.OrderRepository;
import com.tabonfurniture.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    public UserResponse signup(SignupRequest signupRequest) throws Exception {
        // Validate password match
        if (!signupRequest.isPasswordMatching()) {
            throw new Exception("Passwords do not match");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new Exception("Username is already taken");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new Exception("Email is already registered");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(PasswordUtil.hashPassword(signupRequest.getPassword()));
        user.setRole(User.Role.USER);
        
        // Save user
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser);
    }
    
    public UserResponse login(LoginRequest loginRequest) throws Exception {
        // Find user by username or email
        Optional<User> userOptional = userRepository.findByEmail(
            loginRequest.getEmail()
        );
        
        if (userOptional.isEmpty()) {
            throw new Exception("Invalid username/email or password");
        }
        
        User user = userOptional.get();
        
        // Verify password
        if (!PasswordUtil.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
            throw new Exception("Invalid username/email or password");
        }
        
        return new UserResponse(user);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    // Admin methods
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    @Transactional
    public void updateUser(Long id, String username, String email, User.Role role) throws Exception {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Check if username is taken by another user
            if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
                throw new Exception("Username is already taken");
            }
            
            // Check if email is taken by another user
            if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                throw new Exception("Email is already registered");
            }
            
            user.setUsername(username);
            user.setEmail(email);
            user.setRole(role);
            userRepository.save(user);
        } else {
            throw new Exception("User not found");
        }
    }
    
    @Transactional
    public void deleteUser(Long id) throws Exception {
        if (userRepository.existsById(id)) {
            // Delete all related data in the correct order to avoid foreign key constraint violations
            
            // 1. Delete all contacts by this user
            contactRepository.deleteByUserId(id);
            
            // 2. Delete all orders by this user
            orderRepository.deleteByUserId(id);
            
            // 3. Finally delete the user
            userRepository.deleteById(id);
        } else {
            throw new Exception("User not found");
        }
    }
    
    public void resetPassword(String email, String newPassword) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new Exception("User not found with this email");
        }
        
        User user = userOptional.get();
        user.setPassword(PasswordUtil.hashPassword(newPassword));
        userRepository.save(user);
    }
    
    @Transactional
    public void updateProfile(Long userId, String username, String email, String profileImageUrl) throws Exception {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new Exception("User not found");
        }
        
        User user = userOpt.get();
        String oldUsername = user.getUsername();
        
        // Check if username is taken by another user
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new Exception("Username is already taken");
        }
        
        // Check if email is taken by another user
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new Exception("Email is already registered");
        }
        
        user.setUsername(username);
        user.setEmail(email);
        if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
            user.setProfileImageUrl(profileImageUrl);
        }
        
        userRepository.save(user);
    }
    
    @Transactional
    public void updateProfileImage(Long userId, String profileImageUrl) throws Exception {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new Exception("User not found");
        }
        
        User user = userOpt.get();
        user.setProfileImageUrl(profileImageUrl);
        userRepository.save(user);
    }
    
    /**
     * Get the ID of the previous user (lower ID) for navigation
     */
    public Long getPreviousUserId(Long currentUserId) {
        return userRepository.findTopByIdLessThanOrderByIdDesc(currentUserId)
                .map(User::getId)
                .orElse(null);
    }
    
    /**
     * Get the ID of the next user (higher ID) for navigation
     */
    public Long getNextUserId(Long currentUserId) {
        return userRepository.findTopByIdGreaterThanOrderByIdAsc(currentUserId)
                .map(User::getId)
                .orElse(null);
    }
    
    /**
     * Get the position of a user (1-based index) based on ID order
     */
    public int getUserPosition(Long userId) {
        return (int) userRepository.countByIdLessThanEqual(userId);
    }
    
    /**
     * Get the total number of users
     */
    public long getTotalUserCount() {
        return userRepository.count();
    }
}
