package com.lsd.thesaurus.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lsd.thesaurus.model.EntityInfo;

public interface EntityInfoRepository extends JpaRepository<EntityInfo, Integer>{

	Page<EntityInfo> findAll(Pageable pageable);
}
