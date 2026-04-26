package com.mobix.service;

import com.mobix.model.User;
import com.mobix.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private static final String PHONE_REGEX = "^\\d{10}$";
    private static final String STRONG_PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";

    public User registerUser(User user) {
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }

        if (user.getAddress() == null || user.getAddress().trim().isEmpty()) {
            throw new RuntimeException("Address is required");
        }

        if (user.getPhone() == null || !user.getPhone().matches(PHONE_REGEX)) {
            throw new RuntimeException("Phone number must be exactly 10 digits");
        }

        if (user.getPassword() == null || !user.getPassword().matches(STRONG_PASSWORD_REGEX)) {
            throw new RuntimeException("Password must be at least 8 characters and include letters and numbers");
        }

        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        
        // Check if phone already exists
        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new RuntimeException("Phone number already registered");
        }
        
        // Set default role if not provided
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseGet(() -> userRepository.findByEmail(normalizedEmail).orElseGet(() -> userRepository.findAll().stream()
                .filter(user -> user.getEmail() != null && user.getEmail().trim().equalsIgnoreCase(normalizedEmail))
                .findFirst()
                .orElse(null)));
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        if (user != null) {
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());
            user.setAddress(userDetails.getAddress());
            user.setRole(userDetails.getRole());
            return userRepository.save(user);
        }
        return null;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User login(String email, String password) {
        if (email == null || password == null) {
            return null;
        }

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPassword = password.trim();

        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findAll().stream()
                    .filter(user -> user.getEmail() != null && user.getEmail().trim().equalsIgnoreCase(normalizedEmail))
                    .findFirst();
        }

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // In a real application, you should hash passwords
            // For now, we're doing plain text comparison
            if (user.getPassword() != null && user.getPassword().trim().equals(normalizedPassword)) {
                return user;
            }
        }
        return null;
    }
}