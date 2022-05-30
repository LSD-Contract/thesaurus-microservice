package com.lsd.thesaurus.services;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lsd.thesaurus.model.BaseModel;
import com.lsd.thesaurus.model.EntityInfo;
import com.lsd.thesaurus.repository.CategoryRepository;
import com.lsd.thesaurus.repository.ClassificationRepository;
import com.lsd.thesaurus.repository.CatalogueRepository;
import com.lsd.thesaurus.repository.DocumentTypeRepository;
import com.lsd.thesaurus.repository.EntityInfoRepository;
import com.lsd.thesaurus.response.ResponseBean;
import com.lsd.thesaurus.utilities.ILogger;

/**
 * This is service class for CRUD operation of all types of entities
 * 
 * @author Vijayadithya Doddi
 *
 */
@Service
public class CrudServices {

	@Autowired private ILogger logger;
	@PersistenceContext private EntityManager entityManager;
	@Autowired private LoadTransientsService loadTransientsService;
	
	@Autowired private EntityInfoRepository entityInfoRepository;
	@Autowired private CategoryRepository categoryRepository;
	@Autowired private DocumentTypeRepository documentTypeRepository;
	@Autowired private CatalogueRepository catalogueRepository;
	@Autowired private ClassificationRepository classificationRepository;
	
	Map<String, EntityInfo> entityInfoHasMap = new HashMap<String, EntityInfo>();
	Map<String, JpaRepository<?, Integer>> repositoryMap = new HashMap<String, JpaRepository<?, Integer>>();
	String[] entities = { "Catalogue", "DocumentType", "Category", "Classification"};

	@PostConstruct
	public void postInitialize() {
		List<EntityInfo> entityInfos = entityInfoRepository.findAll();
		for (EntityInfo entityInfo : entityInfos) {
			entityInfoHasMap.put(entityInfo.getEntityName(), entityInfo);
		}
		repositoryMap.put("Catalogue",catalogueRepository);
		repositoryMap.put("DocumentType",documentTypeRepository);
		repositoryMap.put("Category",categoryRepository);
		repositoryMap.put("Classification",classificationRepository);
	}

	/**
	 * This method return index value for supported entity if entity not supported
	 * returns negative integer
	 * 
	 * @param entityName
	 * @return Integer
	 */
	private int supportedEntityIndex(String entityName) {

		for (int i = 0; i < entities.length; i++) {
			if (entities[i].equalsIgnoreCase(entityName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * This method saves new entity to database using respective repository
	 * 
	 * @param entityName
	 * @param requestObj
	 * @return ResponseEntity
	 */
	public ResponseBean saveEntity(HttpServletRequest servletRequest, String entityName, Object requestObj) {

		logger.debug(1, this.getClass(),
				"saveEntity #entry request:" + requestObj.toString() + " entityName:" + entityName);
		ResponseBean response = new ResponseBean();

		try {

			int entityIndex = this.supportedEntityIndex(entityName);
			logger.debug(2, this.getClass(), "entityIndex:" + entityIndex);
			if (entityIndex == -1) {
				response.setStatus(HttpStatus.EXPECTATION_FAILED);
				response.setMessage("Invalid entity name");
				return response;
			}

			JpaRepository repository = (JpaRepository) repositoryMap.get(entityName);

			Class<?> entityClass = Class.forName(entityInfoHasMap.get(entityName).getEntityClassName());

			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			Object obj = mapper.readValue(ow.writeValueAsString(requestObj), entityClass);
			BaseModel baseClassObj = (BaseModel) obj;
			
			// create or update
			Field idField = entityClass.getDeclaredField("id");
			idField.setAccessible(true);
			int idValue = (int) idField.get(obj);
			
			if( idValue == 0 ) {			
				baseClassObj.setCreatedBy("System");
				baseClassObj.setCreatedOn(LocalDateTime.now());
			}else {
				// get createdOn and createdBy values;
				String createdBy = null;
				LocalDateTime createdOn = null;
				Optional<Object> currentObjOptional = repository.findById(idValue);
				if (currentObjOptional != null && currentObjOptional.isPresent()) {
					Object currentObject = currentObjOptional.get();
					try {
						Field createdByField = entityClass.getSuperclass().getDeclaredField("createdBy");
						createdByField.setAccessible(true);
						createdBy = (String) createdByField.get(currentObject);
						Field createdOnField = entityClass.getSuperclass().getDeclaredField("createdOn");
						createdOnField.setAccessible(true);
						createdOn = (LocalDateTime) createdOnField.get(currentObject);
					} catch (Exception e) {
						
					}
				}

				if (createdBy != null) {
					baseClassObj.setCreatedBy(createdBy);
				}
				if (createdOn != null) {
					baseClassObj.setCreatedOn(createdOn);
				}
				baseClassObj.setModifiedBy("System");
				baseClassObj.setModifiedOn(LocalDateTime.now());
			}
			
			
			// Check for existing name
			Field nameField = entityClass.getDeclaredField("name");
			nameField.setAccessible(true);
			String nameValue = (String) nameField.get(obj);
			List<BaseModel> baseClassObjList = (List<BaseModel>)repository.findAll();
			
			for(BaseModel baseModelClassObjItr: baseClassObjList) {
				// Check for existing name
				Field nameFieldItr= entityClass.getDeclaredField("name");
				nameFieldItr.setAccessible(true);
				String nameValueItr = (String) nameFieldItr.get(baseModelClassObjItr);
				if(nameValue.equalsIgnoreCase(nameValueItr)) {
					throw new Exception(entityName + " already exists");
				}
			}
			
			logger.debug(2, this.getClass(), "Got object");

			if (repository == null) {
				response.setStatus(HttpStatus.EXPECTATION_FAILED);
				response.setMessage("Entity Repository Not Found");
				return response;
			}

			Object saved = repository.save(baseClassObj);

			if (saved != null) {
				response.setStatus(HttpStatus.OK);
				response.setMessage("Entity Saved Succussfully");
				response.setData(saved);
			} else {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
				response.setMessage("Entity Save failed");
			}

		} catch (Exception e) {
			logger.debug(2, this.getClass(), "Operation failed with error : " + e.getMessage());
			response.setStatus(HttpStatus.EXPECTATION_FAILED);
			response.setMessage("Server failed with error : " + e.getMessage());
			e.printStackTrace();
		}

		logger.debug(1, this.getClass(), "saveEntity #exit, entityName:" + entityName);
		return response;
	}

	/**
	 * This method retrives all the object of requested entity from database
	 * 
	 * @param entityName
	 * @param pageRequest 
	 * @return ResponseEntity<ResponseBean>
	 */
	public ResponseBean getAllEntities(HttpServletRequest servletRequest, String entityName,int pageNumber, int pageSize) {

		logger.debug(1, this.getClass(), "getAllEntities #entry request, entityName:" + entityName);
		
		ResponseBean response = new ResponseBean();
		try {
			response.setStatus(HttpStatus.OK);
			response.setMessage("Entities retrived successfully");
	
			int entityIndex = this.supportedEntityIndex(entityName);
			logger.debug(2, this.getClass(), "entityIndex:" + entityIndex);
			if (entityIndex == -1) {
				response.setStatus(HttpStatus.EXPECTATION_FAILED);
				response.setMessage("Invalid entity name");
				return response;
			}
	
			Class<?> entityClass = Class.forName(entityInfoHasMap.get(entityName).getEntityClassName());
			
			JpaRepository repo = (JpaRepository) repositoryMap.get(entityName);
			if( repo == null ) {
				response.setStatus(HttpStatus.EXPECTATION_FAILED);
				response.setMessage("This service not available for entity "+entityName);
				return response;
			}
			
			List<Object> entities = repo.findAll();
			
			// Call SetTransient method if defined
			InvokeSetTransient (entities, entityName );
			
			response.setData(entities);
		} catch (Exception e) {
			logger.debug(1, this.getClass(), "Operation failed with error : " + e.getMessage());
			response.setStatus(HttpStatus.EXPECTATION_FAILED);
			response.setMessage("Server failed with error : " + e.getMessage());
			e.printStackTrace();
		}
		
		logger.debug(1, this.getClass(), "getAllEntities #exit request, entityName:" + entityName);
		return response;
	}

	/**
	 * This method deletes requested entity from database
	 * 
	 * @param entityName
	 * @param ids
	 * @return ResponseEntity<ResponseBean>
	 */
	public ResponseBean deleteEntity(HttpServletRequest servletRequest, String entityName, String ids) {

		logger.debug(1, this.getClass(), "Delete entry request,  entityName:" + entityName + ", entityId(s):" + ids);
		ResponseBean response = new ResponseBean();
		
		List<String> idsToDeleteStr = Arrays.asList(ids.split(","));
		List<Integer> idsToDelete = new ArrayList<Integer>();
		for (String id : idsToDeleteStr) {
			idsToDelete.add( Integer.parseInt(id) );
		}

		int entityIndex = this.supportedEntityIndex(entityName);
		logger.debug(2, this.getClass(), "entityIndex:" + entityIndex);
		
		if (entityIndex == -1) {
			response.setStatus(HttpStatus.EXPECTATION_FAILED);
			response.setMessage("Invalid entity name");
			return response;
		}
		
		if( !entityInfoHasMap.get(entityName).isDeleteAllowed() ) {
			response.setStatus(HttpStatus.EXPECTATION_FAILED);
			response.setMessage("Deleting not allowed for entity : "+entityName);
			ResponseEntity.status(response.getStatus()).body(response);
		}
		
		JpaRepository repo = (JpaRepository) repositoryMap.get(entityName);

		if ( !idsToDelete.isEmpty() ) {
			repo.deleteAllById(idsToDelete);
			response.setStatus(HttpStatus.OK);
			response.setMessage("Entity Deleted Succussfully");
		} else {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			response.setMessage("Entity deleting failed, no entity found with id : " + ids);
		}

		logger.debug(1, this.getClass(), "getAllEntities #exit request, entityName:" + entityName);
		return response;
	}

	public ResponseBean getByFieldOrPaginated(HttpServletRequest servletRequest, 
			String entityName, String fieldName, String valueToMatch, Integer pageNumber, Integer pageSize) {
		
		logger.info(1, this.getClass(), "getByFieldOrPaginated entry# entityNmae : "+entityName+", field : "+fieldName+", value To Match : "+valueToMatch);
		ResponseBean response = new ResponseBean();
		
		if (fieldName == null || fieldName.isEmpty()) {
			response.setStatus(HttpStatus.EXPECTATION_FAILED);
			response.setMessage("Field name cannot be null");
			return response;			
		}
		
		if (valueToMatch == null || valueToMatch.isEmpty()) {
			response.setStatus(HttpStatus.EXPECTATION_FAILED);
			response.setMessage("valueToMatch cannot be null");
			return response;			
		}
		
		int entityIndex = this.supportedEntityIndex(entityName);
		logger.debug(2, this.getClass(), "entityIndex : " + entityIndex + "entityName : " + entityName);
		
		if (entityIndex == -1) {
			response.setStatus(HttpStatus.EXPECTATION_FAILED);
			response.setMessage("Invalid entity name");
			return response;
		}
		
		
		response.setStatus(HttpStatus.ACCEPTED.OK);
		response.setMessage("Result retrived successfully");
		
		try {
			Pageable pageable = PageRequest.of(pageNumber, pageSize);
			
			if( fieldName == null ||fieldName.isEmpty() ) {
				JpaRepository repo = (JpaRepository) repositoryMap.get(entityName);
				if( repo == null ) {
					response.setStatus(HttpStatus.EXPECTATION_FAILED);
					response.setMessage("This service not available for entity "+entityName);
					return response;
				}
								
				Page<Object> entities = repo.findAll( pageable );
				
				// Call SetTransient method if defined
				InvokeSetTransient (entities.getContent(), entityName );
				
				response.setStatus(HttpStatus.OK);
				response.setMessage("Entities retrived successfully");
				response.setData(entities);
				return response;
			}
			
			String className = entityInfoHasMap.get(entityName).getEntityClassName();
			logger.info(1, this.getClass(), "className : "+className);
			Class<?> entityClass = Class.forName( className );
			Field fieldValue = entityClass.getDeclaredField(fieldName);
			String comparator = "";
			AnnotatedType type = fieldValue.getAnnotatedType();
			logger.info(1, this.getClass(), "type : "+type.getType().toString());
			switch ( type.getType().toString() ) {
			
			case "int":	case "class java.lang.Integer" : case "float": case "class java.lang.Float" :
			case "double": case "class java.lang.Double" : case "boolean":
			case "class java.lang.Boolean" :
				comparator = "="; break;
			case "class java.time.LocalDate":  comparator = "="; valueToMatch = "'"+valueToMatch+"'"; break;
			case "class java.lang.String": case "char": case "class java.lang.Character" : 
				comparator = "Like"; valueToMatch = "'%"+valueToMatch+"%'"; break;
			
			default: comparator = "=";
				break;
			}
			
			String queryStr = "select x from "+entityName+" x where "+fieldName+" "+comparator+" "+valueToMatch;
			logger.info(1, this.getClass(), "getByField Query : "+queryStr);
			TypedQuery<?> query = entityManager.createQuery(queryStr, entityClass);
			
			query.setFirstResult( pageNumber * pageSize );
		    query.setMaxResults( pageSize );
		    
		    String countQueryStr = "select count(x."+fieldName+") from "+entityName+" x where "+fieldName+" "+comparator+" "+valueToMatch;
		    javax.persistence.Query queryTotal = entityManager.createQuery(countQueryStr);
		    long countResult = (long)queryTotal.getSingleResult();
			
			List<Object> result = (List<Object>) query.getResultList();
			
			// Call SetTransient method if defined
			InvokeSetTransient (result, entityName );
			
			response.setData( new PageImpl<Object>(result,pageable,countResult) );
			
			logger.info(1, this.getClass(), "getByFieldOrPaginated exit#");
		}catch (Exception e) {
			logger.debug(1, this.getClass(), "getByFieldOrPaginated API failed with error : "+e.getMessage());
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			response.setMessage("getByField API failed with error : "+e.getMessage());
		}
		
		return response;
	}

	private void InvokeSetTransient (List<Object> entities, String entityName ){
			
		// Call SetTransient method if defined
		if (entities != null && !entities.isEmpty()) {
			try {
				Class<?> entityClass = Class.forName(entityInfoHasMap.get(entityName).getEntityClassName());
				Class<?> cls = loadTransientsService.getClass();
				Method method = cls.getDeclaredMethod(entityName+"SetTransient", entityClass);
				for (Object obj : entities) {
					method.invoke(loadTransientsService, obj);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				System.out.println("Method not defined");
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
