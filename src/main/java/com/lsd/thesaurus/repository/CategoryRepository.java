package com.lsd.thesaurus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsd.thesaurus.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer>{
	
}
