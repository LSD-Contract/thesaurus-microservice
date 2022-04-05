package com.lsd.thesaurus.services;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.lsd.thesaurus.model.Catalogue;
import com.lsd.thesaurus.repository.CatalogueRepository;
import com.lsd.thesaurus.response.ResponseBean;
import com.lsd.thesaurus.utilities.ILogger;

@Service
public class BusinessService {

	@Autowired private ILogger logger;
	@Autowired private CatalogueRepository catalogueRepository;
		
	@PostConstruct
	public void postInitialize() {

	}
    
	public ResponseBean echo(HttpServletRequest servletRrequest, String message) {
		
		logger.debug(1, this.getClass(), "echo entry# message:" + message);
		ResponseBean responseBean = new ResponseBean();
		try {
			responseBean.setStatus(HttpStatus.OK);
			responseBean.setMessage("echoed successfully");
			
			responseBean.setData("Hello "+ message);
		
		} catch (Exception e) {
			e.printStackTrace();
			responseBean.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			responseBean.setMessage("Failed to echo");
			logger.error(1, this.getClass(), "echo API failed with exception:"+e.getMessage());
		}
		
		logger.debug(1, this.getClass(), "echo exit#");
		return responseBean;
	}	
	
	public ResponseBean addCatalogueDocument(HttpServletRequest servletRrequest, int catalogueId, MultipartFile file) {
		
		logger.debug(1, this.getClass(), "addCatalogue entry# ");
		ResponseBean responseBean = new ResponseBean();
		try {
			responseBean.setStatus(HttpStatus.OK);
			responseBean.setMessage("Catalogue created successfully");
			
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			Path targetLocation = FileSystems.getDefault().getPath("", fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
		
		} catch (Exception e) {
			e.printStackTrace();
			responseBean.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			responseBean.setMessage("Failed to echo");
			logger.error(1, this.getClass(), "echo API failed with exception:"+e.getMessage());
		}
		
		logger.debug(1, this.getClass(), "echo exit#");
		return responseBean;
	}
	
	public ResponseBean uploadCatalogueFile(HttpServletRequest servletRrequest, Catalogue catalogue, MultipartFile file) {
		
		logger.debug(1, this.getClass(), "addCatalogue entry# ");
		ResponseBean responseBean = new ResponseBean();
		try {
			responseBean.setStatus(HttpStatus.OK);
			responseBean.setMessage("Catalogue created successfully");
			
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			Path targetLocation = FileSystems.getDefault().getPath("", fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			
			Catalogue savedCatalogue = catalogueRepository.save(catalogue);
			
			responseBean.setData(savedCatalogue);
		
		} catch (Exception e) {
			e.printStackTrace();
			responseBean.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			responseBean.setMessage("Failed to echo");
			logger.error(1, this.getClass(), "echo API failed with exception:"+e.getMessage());
		}
		
		logger.debug(1, this.getClass(), "echo exit#");
		return responseBean;
	}
}