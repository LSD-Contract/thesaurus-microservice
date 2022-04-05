package com.lsd.thesaurus.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lsd.thesaurus.utilities.ILogger;

@Service
public class LoadTransientsService {

	@Autowired private ILogger logger;
	
	@PostConstruct
	public void postInitialize() {

	}
    
}