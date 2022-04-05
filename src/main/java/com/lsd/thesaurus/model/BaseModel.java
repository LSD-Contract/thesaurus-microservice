package com.lsd.thesaurus.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * This is base class that must be extended by each entity so that each entity will have fields id, createdBy, createdOn, modifiedBy, modifiedOn
 * these fields will help in auditing the records.
 *
 *  @author Vijayadithya Doddi
 */

@MappedSuperclass
public class BaseModel {

	@Column(updatable = false)
	private String createdBy;

	@Column(updatable = false)
	private LocalDateTime createdOn;

	@Column(insertable = false)
	private String modifiedBy;

	@Column(insertable = false)
	private LocalDateTime modifiedOn;

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public LocalDateTime getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(LocalDateTime modifiedOn) {
		this.modifiedOn = modifiedOn;
	}
}
