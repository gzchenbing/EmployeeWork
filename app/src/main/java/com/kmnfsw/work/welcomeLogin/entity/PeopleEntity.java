package com.kmnfsw.work.welcomeLogin.entity;

public class PeopleEntity {
	private String peopleno;
	private String password;
	private String name;
	public PeopleEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PeopleEntity(String peopleno, String password, String name) {
		super();
		this.peopleno = peopleno;
		this.password = password;
		this.name = name;
	}
	public String getPeopleno() {
		return peopleno;
	}
	public void setPeopleno(String peopleno) {
		this.peopleno = peopleno;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return "PeopleEntity [peopleno=" + peopleno + ", password=" + password + ", name=" + name + "]";
	}
	
}
