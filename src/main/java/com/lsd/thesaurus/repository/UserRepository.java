package com.lsd.thesaurus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsd.thesaurus.model.User;

public interface UserRepository extends JpaRepository<User, Integer>{
	
	List<User> findByFullName(String fullName);
	List<User> findByFullNameAndPwd(String fullName, String password);
}
