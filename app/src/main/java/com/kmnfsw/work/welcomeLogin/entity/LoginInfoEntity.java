package com.kmnfsw.work.welcomeLogin.entity;

/**用于登录效验传参*/
public class LoginInfoEntity {
	
	private String peopleno;
	private String password;
	private String devicemac;
	public LoginInfoEntity(String peopleno, String password, String devicemac) {
		super();
		this.peopleno = peopleno;
		this.password = password;
		this.devicemac = devicemac;
	}
	public LoginInfoEntity() {
		super();
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
	public String getMac() {
		return devicemac;
	}
	public void setMac(String devicemac) {
		this.devicemac = devicemac;
	}
	
	
	

}
