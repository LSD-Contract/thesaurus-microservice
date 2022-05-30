package com.lsd.thesaurus.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lsd.thesaurus.model.Users;
import com.lsd.thesaurus.response.ResponseBean;
import com.lsd.thesaurus.services.BusinessService;
import com.lsd.thesaurus.utilities.ILogger;

@CrossOrigin
@RestController
@RequestMapping("/thesaurus")
public class BusinessController {

	@Autowired ILogger logger;
	@Autowired BusinessService businessService;

	@GetMapping(value = "business/echo", produces = { "application/json" })
	public ResponseBean echo(HttpServletRequest servletRequest, 
			@RequestParam(value = "message", required = true) String message) throws Exception {
		setThreadLocal(servletRequest);

		logger.debug(1, this.getClass(), "echo# message:" + message);

		return businessService.echo(servletRequest, message);
	}
    
    @PostMapping(value = "business/addCatalogueDocument", produces = { "application/json" })
    public ResponseBean addCatalogueDocument(HttpServletRequest servletRequest,
    		@RequestParam (value = "catalogueId", required = true) int catalogueId,
    		@RequestBody MultipartFile file
    		) {

		//logger.debug(1, this.getClass(), "echo# catalogueId:" + catalogueId);
		//int catalogueId = 1;
		//MultipartFile file=null;
    	return businessService.addCatalogueDocument(servletRequest, catalogueId, file);
    }
    
    /*
	@GetMapping(value = "business/downloadDocument", produces = { "application/jpg" })
	public ResponseEntity<Resource> downloadDocument(HttpServletRequest servletRequest,
			@RequestParam (value = "fileName",required = true) String fileName) throws Exception {
		setThreadLocal(servletRequest);

		logger.debug(1, this.getClass(), "downloadDocument#");
		
		return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,fileName )
                .contentType(MediaType.parseMediaType("application/jpg"))
                .body(this.businessService.downloadDocument( fileName ));
	}
	*/
    
	@GetMapping(value = "business/downloadDocument")
	public ResponseEntity<InputStreamResource> downloadDocument (HttpServletRequest httpServletRequest,
			@RequestParam (value = "documentName",required = true) String documentName) {

		logger.debug(1, this.getClass(), "downloadDocument# documentName:"+documentName);
		
		return businessService.downloadDocument(httpServletRequest, documentName);
    }
	
	@GetMapping(value = "business/credentials", produces = { "application/json" })
	public ResponseBean credentials(HttpServletRequest servletRequest, 
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password) throws Exception {
		setThreadLocal(servletRequest);

		logger.debug(1, this.getClass(), "credentials# username:" + username);

		return businessService.credentials(servletRequest, username, password);
	}
	
	@PostMapping(value = "business/create/credentials", produces = { "application/json" })
	public ResponseBean createCredentials(HttpServletRequest servletRequest, 
			@RequestBody Users user) throws Exception {
		setThreadLocal(servletRequest);

		logger.debug(1, this.getClass(), "createCredentials# user:" + user);

		return businessService.createCredentials(servletRequest, user);
	}
	
	
	/**
	 * Created a thread local and sets employee name for use by logger
	 *
	 * @param servletRequest
	 */
	private void setThreadLocal(HttpServletRequest servletRequest) {
		String employeeName = (String) servletRequest.getAttribute("employeeName");

		ThreadLocal<String> loggerThreadLocal = new ThreadLocal<String>();
		loggerThreadLocal.set(employeeName);
		logger.setThreadLocal(loggerThreadLocal);
	}
}
