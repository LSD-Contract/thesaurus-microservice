package com.lsd.thesaurus.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.lsd.thesaurus.utilities.EntityListener;

@Entity
@Table(name = "Users")
@EntityListeners(EntityListener.class)
public class Users extends BaseModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String username;
	private String password;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isPresent() {
		if((this.username == null || this.username == "") && 
				(this.password == null || this.password == "")) {
			return false;
		}
		return true;
	}

}
