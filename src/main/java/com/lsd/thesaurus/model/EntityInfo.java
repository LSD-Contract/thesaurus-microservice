package com.lsd.thesaurus.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "entityInfo")
//@EntityListeners(EntityListener.class)
public class EntityInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String entityName;
	private String entityClassName;
	private boolean deleteAllowed;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public String getEntityClassName() {
		return entityClassName;
	}
	public void setEntityClassName(String entityClassName) {
		this.entityClassName = entityClassName;
	}
	public boolean isDeleteAllowed() {
		return deleteAllowed;
	}
	public void setDeleteAllowed(boolean deleteAllowed) {
		this.deleteAllowed = deleteAllowed;
	}

}
