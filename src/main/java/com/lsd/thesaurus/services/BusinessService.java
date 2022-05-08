package com.lsd.thesaurus.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.lsd.thesaurus.model.Catalogue;
import com.lsd.thesaurus.repository.CatalogueRepository;
import com.lsd.thesaurus.response.ResponseBean;
import com.lsd.thesaurus.utilities.ILogger;

@Service
public class BusinessService {

	@Autowired
	@Value("${filesDir}") 
	public String filesDir;
	
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
	
	/**
	 * Add a catelogue document
	 * @param servletRrequest
	 * @param catalogueId
	 * @param file
	 * @return
	 */
	public ResponseBean addCatalogueDocument(HttpServletRequest servletRrequest, int catalogueId, MultipartFile file) {
		
		logger.debug(1, this.getClass(), "addCatalogue entry# ");
		ResponseBean responseBean = new ResponseBean();
		try {
			responseBean.setStatus(HttpStatus.OK);
			responseBean.setMessage("Catalogue created successfully");
			
			Optional<Catalogue> catalogueOptional = catalogueRepository.findById(catalogueId);
			if (catalogueOptional == null || !catalogueOptional.isPresent()) {
				responseBean.setStatus(HttpStatus.EXPECTATION_FAILED);
				responseBean.setMessage("Invalid catalogue Id");
				return responseBean;
			}

			Catalogue catalogue = catalogueOptional.get();
			
			String sourceFileName = StringUtils.cleanPath(file.getOriginalFilename());
			String targetFileName = System.currentTimeMillis() + "";
			String extension = sourceFileName.substring(sourceFileName.lastIndexOf("."), sourceFileName.length());
			Path targetLocation = FileSystems.getDefault().getPath(filesDir, targetFileName + extension);
			Files.copy(file.getInputStream(), targetLocation);
		
			catalogue.setSavedfilename(targetFileName + extension);
			catalogue.setDocumentName(sourceFileName);
			
			Catalogue savedCatalogue = catalogueRepository.save(catalogue);
			
			responseBean.setData(savedCatalogue);
		} catch (Exception e) {
			e.printStackTrace();
			responseBean.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			responseBean.setMessage("failed to add Catalogue Document");
			logger.error(1, this.getClass(), "addCatalogueDocument failed with exception:"+e.getMessage());
		}
		
		logger.debug(1, this.getClass(), "addCatalogueDocument exit#");
		return responseBean;
	}
	

	/**
	 * download a catalogue document
	 * @param httpServletRequest
	 * @param documentName
	 * @return
	 */
	public ResponseEntity<InputStreamResource> downloadDocument (HttpServletRequest httpServletRequest,
			String documentName) {
		final HttpHeaders headers = new HttpHeaders();

		try {
			headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
			headers.add(HttpHeaders.PRAGMA, "no-cache");
			headers.add(HttpHeaders.EXPIRES, "0");

			File documentToDownload = new File(filesDir + "/" + documentName);
			boolean exists = documentToDownload.exists();
			
			if (!exists) {
			    logger.error(1, getClass(), "Document "+documentName + " not found");
			    return ResponseEntity.notFound().headers(headers).build();
			}

			InputStreamResource resource = null;
			long contentLength = 0;
			contentLength = documentToDownload.length();
			resource = new InputStreamResource(new FileInputStream(documentToDownload));

			/*
			if (resource == null) {
			    return ResponseEntity.notFound().headers(headers).build();
			}
			*/
     
			return ResponseEntity.ok()
			        .headers(headers)
			        .contentLength(contentLength)
			        .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
			        .body(resource);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error(1, getClass(), "Document "+documentName + " not found");
			return ResponseEntity.notFound().headers(headers).build();
		}
	}
	
	/*
	public Resource downloadDocument(String fileName) throws Exception {
		
		logger.info(1, this.getClass(), "downloadDocument entry#");
		return loadFile(fileName);
	
	}
	
	private Resource loadFile(String fileName) throws Exception {
		File filePath = new File(filesDir, fileName);
		if(filePath.exists())
			return new FileSystemResource(filePath);
		else 
			throw new Exception("Requested file not found : "+fileName);

	}
	*/
}