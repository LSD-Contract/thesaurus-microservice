package com.lsd.thesaurus.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.lsd.thesaurus.utilities.EntityListener;

@Entity
@Table(name = "documenttype")
@EntityListeners(EntityListener.class)
public class DocumentType extends BaseModel {

	public static int DOCUMENT_TYPE_IMAGE = 1;
	public static int DOCUMENT_TYPE_WORD = 2;
	public static int DOCUMENT_TYPE_PDF = 3;
	public static int DOCUMENT_TYPE_VIDEO = 4;
	public static int DOCUMENT_TYPE_AUDIO = 5;
	public static int DOCUMENT_TYPE_TEXT = 6;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	
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
}
