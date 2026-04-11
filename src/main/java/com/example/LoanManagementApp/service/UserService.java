package com.example.LoanManagementApp.service;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.LoanManagementApp.DTO.LoginDTO;
import com.example.LoanManagementApp.DTO.LoginResponse;
import com.example.LoanManagementApp.DTO.UserSignupDTO;
import com.example.LoanManagementApp.model.Branch;
import com.example.LoanManagementApp.model.Users;
import com.example.LoanManagementApp.repo.BranchRepo;
import com.example.LoanManagementApp.repo.UserRepo;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private BranchRepo branchRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * 🔐 User Registration (Signup) with DTO
     * Converts UserSignupDTO to Users entity and saves to database
     */
    public Users register(UserSignupDTO signupDTO) {
        // Check if username or email already exists
        if (repo.findByUsername(signupDTO.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }

        if (repo.findByEmail(signupDTO.getEmail()) != null) {
            throw new RuntimeException("Email already registered");
        }

        // Find branch by name (provided by frontend)
        Branch branch = branchRepo.findByBranchName(signupDTO.getBranch())
                .orElseThrow(() -> new RuntimeException("Branch not found: " + signupDTO.getBranch()));

        // Create Users entity from DTO
        Users user = new Users();
        user.setUsername(signupDTO.getUsername());
        user.setEmail(signupDTO.getEmail());
        user.setPassword(encoder.encode(signupDTO.getPassword()));
        user.setPhoneNumber(signupDTO.getPhoneNumber());
        user.setAdharNumber(signupDTO.getAdharNumber());
        user.setDob(signupDTO.getDob());
        user.setGender(signupDTO.getGender().toUpperCase());
        user.setBranch(branch);
        user.setIsActive(true);

        // Save user to database
        return repo.save(user);
    }

    /**
     * 🔐 User Registration (Signup) with Users entity (for backward compatibility)
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
     * 🔓 User Login Verification with LoginDTO
     */
    public LoginResponse login(LoginDTO loginDTO) {
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                )
        );

        if (!authentication.isAuthenticated()) {
            throw new RuntimeException("Invalid username or password");
        }

        // Fetch full user data from DB
        Users dbUser = repo.findByUsername(loginDTO.getUsername());
        if (dbUser == null) {
            throw new RuntimeException("User not found");
        }

        // Generate JWT
        String token = jwtService.generateToken(loginDTO.getUsername());

        // Return token + userId
        return new LoginResponse(token, dbUser.getId());
    }


    /**
     * 🧾 Get user details by username
     */
    public Users getUser(String username) {
        return repo.findByUsername(username);
    }
}
