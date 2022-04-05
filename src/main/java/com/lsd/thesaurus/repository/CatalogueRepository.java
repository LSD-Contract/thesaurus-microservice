package com.lsd.thesaurus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsd.thesaurus.model.Catalogue;

public interface CatalogueRepository extends JpaRepository<Catalogue, Integer>{
	
}
