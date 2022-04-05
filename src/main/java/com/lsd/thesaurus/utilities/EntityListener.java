package com.lsd.thesaurus.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.lsd.thesaurus.model.Audit;

@Component
public class EntityListener {

	private static String CREATE_OPERATION = "Create";
	private static String UPDATE_OPERATION = "Update";
	private static String DELETE_OPERATION = "Delete";
	private static String CREATED_ON_FIELD = "createdOn";
	private static String MODIFIED_ON_FIELD = "modifiedOn";
	private static String CREATED_BY_FIELD = "createdBy";
	private static String DATABASE_URL = "spring.datasource.url";
	private static String DATABASE_USERNAME = "spring.datasource.username";
	private static String DATABASE_PASSWORD = "spring.datasource.password";

	private static String LOAD_TRANSIENT_VARIABLES_METHOD = "loadTransientVariables";

	@Autowired Environment environment;

	/**
	 * This method is called before an object is persisted
	 * @param obj : object to be persisted
	 */
	@PrePersist
	void onPrePersist(Object obj) {
		try {
			// Set 'created on' field
			Class<?> cls = obj.getClass();
			Field field = cls.getSuperclass().getDeclaredField(CREATED_ON_FIELD);
			if (field != null) {
				field.setAccessible(true);
				field.set(obj, LocalDateTime.now());
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called after an object is persisted
	 * @param obj : object to be persisted
	 */
	@PostPersist
	void onPostPersist(Object obj) {
		loadTransientVariables(obj);
		try {
			CreateAuditEntry(obj, CREATE_OPERATION);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called after an object is loaded
	 * @param obj
	 */
	@PostLoad
	void onPostLoad(Object obj) {
		loadTransientVariables(obj);
	}

	@PreUpdate
	void onPreUpdate(Object obj) {
		try {
			Class<?> cls = obj.getClass();
			Field field = cls.getSuperclass().getDeclaredField(MODIFIED_ON_FIELD);
			if (field != null) {
				field.setAccessible(true);
				field.set(obj, LocalDateTime.now());
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@PostUpdate
	void onPostUpdate(Object obj) {
		loadTransientVariables(obj);

		try {
			CreateAuditEntry(obj, UPDATE_OPERATION);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@PreRemove
	void onPreRemove(Object obj) {
		//System.out.println("LmsEntityListener.onPreRemove(): " + obj);
	}

	@PostRemove
	void onPostRemove(Object obj) {
		try {
			CreateAuditEntry(obj, DELETE_OPERATION);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * creates an audit entity based on the entries in audit object
	 * @param obj - the object to be audited
	 * @param audit - the audit object to be created
	 * @throws Exception
	 */
	private void CreateAuditEntry(Object obj, String operation) throws Exception {

		Connection conn = null;
		Statement stmt = null;

		try {
			Audit audit = new Audit();
			audit.setOperation(operation);

			String entity = obj.getClass().getName();
			audit.setEntity(entity.substring(entity.lastIndexOf(".")+1, entity.length()));
			//audit.setData(new Gson().toJson(obj));
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JSR310Module());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			audit.setData(mapper.writeValueAsString(obj));
			Class<?> cls = obj.getClass();
			Field field = cls.getDeclaredField("id");
			if (field != null) {
				field.setAccessible(true);
				audit.setEntityId((int)field.get(obj));
			}
			field = cls.getSuperclass().getDeclaredField(CREATED_BY_FIELD);
			if (field != null) {
				field.setAccessible(true);
				audit.setCreatedBy((String)field.get(obj));
			}
			audit.setCreatedOn(LocalDateTime.now());
			String url = environment.getProperty(DATABASE_URL);
			String user = environment.getProperty(DATABASE_USERNAME);
			String password = environment.getProperty(DATABASE_PASSWORD);
			
			// Convert createdOn field to proper string
			String createdOnStr = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(audit.getCreatedOn());
			
			String query = "insert into audit (entity, entityId, operation, data, createdBy, createdOn) values (" +
							"'"+audit.getEntity()+"'"+","+audit.getEntityId()+","+
							"'"+audit.getOperation()+"'"+","+"'"+audit.getData()+"'"+","+
							"'"+audit.getCreatedBy()+"'"+","+"'"+createdOnStr+"'"+
							")";

			System.out.println("query:"+query);
			conn = DriverManager.getConnection(url, user, password);
			stmt = conn.createStatement();
			stmt.execute(query);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw e;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw e;
		} catch (SecurityException e) {
			e.printStackTrace();
			throw e;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw e;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw e;
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();		
			}
		}
	}

	private void loadTransientVariables(Object obj) {
		// If any class has 'loadTransientVariables' method, invoke the same
		Class<?> cls = obj.getClass();
		try {
			Method method = cls.getMethod(LOAD_TRANSIENT_VARIABLES_METHOD);
			method.invoke(obj);
		} catch (NoSuchMethodException | SecurityException e) {
			// OK, method may not exist
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
