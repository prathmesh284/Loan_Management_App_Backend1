package com.example.LoanManagementApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.example.LoanManagementApp.DTO.LoginDTO;
import com.example.LoanManagementApp.DTO.LoginResponse;
import com.example.LoanManagementApp.DTO.UserSignupDTO;
import com.example.LoanManagementApp.model.Users;
import com.example.LoanManagementApp.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@PostMapping("/signup")
    public ResponseEntity<Users> register(@Valid @RequestBody UserSignupDTO signupDTO) {
        return ResponseEntity.ok(userService.register(signupDTO));
    }

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
	    return ResponseEntity.ok(userService.login(loginDTO));
	}

}
