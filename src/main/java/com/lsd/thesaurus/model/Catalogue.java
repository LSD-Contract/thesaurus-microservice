package com.lsd.thesaurus.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catalogue")
//@EntityListeners(EntityListener.class)
public class Catalogue extends BaseModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	private int categoryId;
	private int documentTypeId;
	private String documentName;
	private String author;
	private String citation;
	private Integer classificationId;
	private String alternateCategory;
	private String savedfilename;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public int getDocumentTypeId() {
		return documentTypeId;
	}
	public void setDocumentTypeId(int documentTypeId) {
		this.documentTypeId = documentTypeId;
	}
	public String getDocumentName() {
		return documentName;
	}
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getCitation() {
		return citation;
	}
	public void setCitation(String citation) {
		this.citation = citation;
	}
	public Integer getClassificationId() {
		return classificationId;
	}
	public void setClassificationId(Integer classificationId) {
		this.classificationId = classificationId;
	}
	public String getAlternateCategory() {
		return alternateCategory;
	}
	public void setAlternateCategory(String alternateCategory) {
		this.alternateCategory = alternateCategory;
	}
	public String getSavedfilename() {
		return savedfilename;
	}
	public void setSavedfilename(String savedfilename) {
		this.savedfilename = savedfilename;
	}
}
