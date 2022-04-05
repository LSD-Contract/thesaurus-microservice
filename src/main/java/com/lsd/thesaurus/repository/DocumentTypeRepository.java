package com.lsd.thesaurus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsd.thesaurus.model.DocumentType;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Integer>{
	
}
