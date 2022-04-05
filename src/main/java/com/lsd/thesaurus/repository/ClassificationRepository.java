package com.lsd.thesaurus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsd.thesaurus.model.Classification;

public interface ClassificationRepository extends JpaRepository<Classification, Integer> {

}
