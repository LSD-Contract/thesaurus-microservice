package com.lsd.thesaurus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsd.thesaurus.model.Users;

public interface UsersRepository extends JpaRepository<Users, Integer>{
	
	Users findByUsernameAndPassword(String username, String password);
	Users findByUsername(String username);
}
