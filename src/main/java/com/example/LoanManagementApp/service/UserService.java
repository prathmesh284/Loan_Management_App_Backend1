package com.example.LoanManagementApp.service;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.LoanManagementApp.DTO.LoginResponse;
import com.example.LoanManagementApp.model.Users;
import com.example.LoanManagementApp.repo.UserRepo;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * 🔐 User Registration (Signup)
     */
    public Users register(Users user) {
        // Check if username or email already exists
        if (repo.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }

        if (repo.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email already registered");
        }

        // Encode password before saving
        user.setPassword(encoder.encode(user.getPassword()));

        // Save user to database
        return repo.save(user);
    }

    /**
     * 🔓 User Login Verification
     */
    public LoginResponse login(Users user) {
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );

        if (!authentication.isAuthenticated()) {
            throw new RuntimeException("Invalid username or password");
        }

        // Fetch full user data from DB
        Users dbUser = repo.findByUsername(user.getUsername());
        if (dbUser == null) {
            throw new RuntimeException("User not found");
        }

        // Generate JWT
        String token = jwtService.generateToken(user.getUsername());

        // Return token + branchId
        return new LoginResponse(token, dbUser.getId());
    }


    /**
     * 🧾 Get user details by username
     */
    public Users getUser(String username) {
        return repo.findByUsername(username);
    }
}
