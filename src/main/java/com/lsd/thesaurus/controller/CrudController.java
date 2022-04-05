package com.lsd.thesaurus.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lsd.thesaurus.response.ResponseBean;
import com.lsd.thesaurus.utilities.ILogger;
import com.lsd.thesaurus.services.CrudServices;

/**
 * This class represents REST controller
 * @author Vijayadithya Doddi
 */
@CrossOrigin
@RestController
@RequestMapping("/thesaurus/crud")
public class CrudController {
	
	@Autowired ILogger logger;

	@Autowired	private CrudServices crudServices;

	@PostMapping(value = "/create")
	public ResponseBean createNewEntity(HttpServletRequest servletRequest,
			@RequestParam(value = "entityName", required = true) String entityName,
			@RequestBody Object requestObj){
		logger.info(1, this.getClass(), "createNewEntity #entry entityName : "+entityName+" requestObj : "+requestObj );
		return crudServices.saveEntity(servletRequest, entityName, requestObj);
	}

	@PutMapping("/update")
	public ResponseBean updateExistingEntity(HttpServletRequest servletRequest,
			@RequestParam(value = "entityName", required = true) String entityName,
			@RequestBody Object requestObj){
		logger.info(1, this.getClass(), "updateExistingEntity #entry entityName : "+entityName+" requestObj : "+requestObj );
		return crudServices.saveEntity(servletRequest, entityName, requestObj);
	}

	@DeleteMapping("/delete")
	public ResponseBean deleteEntityById(HttpServletRequest servletRequest,
			@RequestParam(value = "ids",required = true)String ids,
			@RequestParam(value = "entityName", required = true) String entityName){
		logger.info(1, this.getClass(), "deleteEntityById #entry entityName : "+entityName+" for id(s) : "+ids);
		return this.crudServices.deleteEntity(servletRequest, entityName, ids);
	}

	@GetMapping("/getAll")
	public ResponseBean getAllEntities(HttpServletRequest servletRequest,
			@RequestParam(value = "entityName",required = true) String entityName){
		logger.info(1, this.getClass(), "getAllEntities #entry entityName : "+entityName);
		return this.crudServices.getAllEntities(servletRequest, entityName,0,0);
	}
	
	@GetMapping("/getAllWithPagination")
	public ResponseBean getAllEntitiesPaginatedOrSerchField(HttpServletRequest servletRequest,
			@RequestParam("entityName") String entityName,
			@RequestParam(value = "fieldName",required = false)String fieldName,
			@RequestParam(value = "valueToMatch", required = false)String valueToMatch,
			@RequestParam(value = "pageNumber",required = false)Integer pageNumber,
			@RequestParam(value = "pageSize", required = false)Integer pageSize){
		
		if (pageNumber == null) {
			pageNumber = 0;
		}
		if (pageSize == null) {
			pageSize = 10;
		}
		
		logger.info(1, this.getClass(), "getAllEntities #entry entityName : "+entityName);
		return this.crudServices.getByFieldOrPaginated(servletRequest, entityName, fieldName, valueToMatch, pageNumber, pageSize);
	}
	
}
