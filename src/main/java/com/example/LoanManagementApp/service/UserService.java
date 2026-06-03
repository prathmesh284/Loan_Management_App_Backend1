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

import java.util.Map;

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

    @Autowired
    private UserOtpService userOtpService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * 🔐 User Registration (Signup) with DTO
     * Converts UserSignupDTO to Users entity and saves to database
     */
    public Map<String, Object> register(UserSignupDTO signupDTO) {
        // Normalize phone early and use normalized value for duplicate checks
        String normalizedPhone = normalizePhone(signupDTO.getPhoneNumber());

        // Check if username or email or phone already exists
        if (repo.findByUsername(signupDTO.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }

        if (repo.findByEmail(signupDTO.getEmail()) != null) {
            throw new RuntimeException("Email already registered");
        }

        if (repo.findByPhoneNumber(normalizedPhone) != null) {
            throw new RuntimeException("Phone number already registered");
        }

        // Find branch by name (provided by frontend)
        Branch branch = branchRepo.findByBranchName(signupDTO.getBranch())
                .orElseThrow(() -> new RuntimeException("Branch not found: " + signupDTO.getBranch()));

        // Create Users entity from DTO
        Users user = new Users();
        user.setUsername(signupDTO.getUsername());
        user.setEmail(signupDTO.getEmail());
        user.setPassword(encoder.encode(signupDTO.getPassword()));
        user.setPhoneNumber(normalizedPhone);
        user.setAdharNumber(signupDTO.getAdharNumber());
        user.setDob(signupDTO.getDob());
        user.setGender(signupDTO.getGender().toUpperCase());
        user.setBranch(branch);
        user.setIsActive(true);
        user.setIsPhoneVerified(false);
        user.setPhoneVerifiedAt(null);

        // Save user to database
        Users saved = repo.save(user);
        Map<String, Object> otpResponse = userOtpService.sendSignupOtp(saved);
        return Map.of(
                "success", true,
                "message", "User created successfully. OTP sent for phone verification.",
                "user", saved,
                "otp", otpResponse
        );
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            throw new RuntimeException("Phone number cannot be null");
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        }
        if (digits.length() == 10) {
            return digits;
        }
        throw new RuntimeException("Phone number must be a valid 10-digit Indian number after normalization");
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

    public Map<String, Object> verifySignupOtp(String phoneNumber, String otpCode) {
        return userOtpService.verifySignupOtp(phoneNumber, otpCode);
    }

    public Map<String, Object> resendSignupOtp(String phoneNumber) {
        return userOtpService.resendSignupOtp(phoneNumber);
    }


    /**
     * 🧾 Get user details by username
     */
    public Users getUser(String username) {
        return repo.findByUsername(username);
    }
}
