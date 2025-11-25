package com.example.LoanManagementApp.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.LoanManagementApp.model.UserPrincipal;
import com.example.LoanManagementApp.model.Users;
import com.example.LoanManagementApp.repo.UserRepo;
import com.example.LoanManagementApp.repo.*;
import com.example.LoanManagementApp.repo.*;
@Service
public class MyUserDetailsService implements UserDetailsService{

	@Autowired
	private UserRepo repo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		Users user = repo.findByUsername(username);
		
		if(user == null) {
			System.out.println("User not found");
			throw new UsernameNotFoundException("user not found");
		}
		
		return new UserPrincipal(user);
	}
	
}
