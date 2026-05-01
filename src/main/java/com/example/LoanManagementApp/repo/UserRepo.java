package com.example.LoanManagementApp.repo;

import com.example.LoanManagementApp.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EnableJpaRepositories
public interface UserRepo extends JpaRepository<Users,Integer>{
	Users findByUsername(String username);
	Users findByEmail(String email);
	Users findByPhoneNumber(String phoneNumber);
}
