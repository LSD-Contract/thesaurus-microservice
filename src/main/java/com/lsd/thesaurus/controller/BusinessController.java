package com.lsd.thesaurus.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    
    @PostMapping("business/addCatalogueDocument")
    public ResponseBean addCatalogueDocument(HttpServletRequest servletRequest,
    		@RequestParam ("catalogueId") int catalogueId,
    		@RequestBody MultipartFile file) {

		logger.debug(1, this.getClass(), "echo# message:" + catalogueId);

    	return businessService.addCatalogueDocument(servletRequest, catalogueId, file);
    }
    
    @PostMapping("/uploadFile")
    public void uploadFile(@RequestPart("file") MultipartFile file) {
        /*
    	String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
        */
    	System.out.println(file.getName());
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
