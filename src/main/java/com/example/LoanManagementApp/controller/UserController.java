package com.example.LoanManagementApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.example.LoanManagementApp.DTO.LoginDTO;
import com.example.LoanManagementApp.DTO.LoginResponse;
import com.example.LoanManagementApp.DTO.UserSignupDTO;
import com.example.LoanManagementApp.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody UserSignupDTO signupDTO) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(signupDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifySignupOtp(@RequestBody Map<String, Object> request) {
        try {
            String phoneNumber = String.valueOf(request.getOrDefault("phoneNumber", "")).trim();
            String otpCode = String.valueOf(request.getOrDefault("otpCode", "")).trim();
            if (phoneNumber.isEmpty() || otpCode.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "phoneNumber and otpCode are required"
                ));
            }
            return ResponseEntity.ok(userService.verifySignupOtp(phoneNumber, otpCode));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendSignupOtp(@RequestBody Map<String, Object> request) {
        try {
            String phoneNumber = String.valueOf(request.getOrDefault("phoneNumber", "")).trim();
            if (phoneNumber.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "phoneNumber is required"
                ));
            }
            return ResponseEntity.ok(userService.resendSignupOtp(phoneNumber));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
	        return ResponseEntity.ok(userService.login(loginDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
	}

}
