package com.kmnfsw.work.sign.entity;

import java.io.Serializable;
import java.util.Date;

public class SignLocationEntity {

	public String  pointno;//巡检点编号
	public String name;
	  
	public double  pointlnggd  ;//高德经度
	  
	public double  pointlatdg ;//高德纬度
	    
	public String peopleno; //员工编号
	  
	public String checktaskno;   //任务编号
	  
	public String signdate;//巡逻工签到时间 

	public String lnorganizeid;

	public SignLocationEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SignLocationEntity(String pointno, String name, double pointlnggd, double pointlatdg, String peopleno,
			String checktaskno, String signdate, String lnorganizeid) {
		super();
		this.pointno = pointno;
		this.name = name;
		this.pointlnggd = pointlnggd;
		this.pointlatdg = pointlatdg;
		this.peopleno = peopleno;
		this.checktaskno = checktaskno;
		this.signdate = signdate;
		this.lnorganizeid = lnorganizeid;
	}

	public String getPointno() {
		return pointno;
	}

	public void setPointno(String pointno) {
		this.pointno = pointno;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPointlnggd() {
		return pointlnggd;
	}

	public void setPointlnggd(double pointlnggd) {
		this.pointlnggd = pointlnggd;
	}

	public double getPointlatdg() {
		return pointlatdg;
	}

	public void setPointlatdg(double pointlatdg) {
		this.pointlatdg = pointlatdg;
	}

	public String getPeopleno() {
		return peopleno;
	}

	public void setPeopleno(String peopleno) {
		this.peopleno = peopleno;
	}

	public String getChecktaskno() {
		return checktaskno;
	}

	public void setChecktaskno(String checktaskno) {
		this.checktaskno = checktaskno;
	}

	public String getSigndate() {
		return signdate;
	}

	public void setSigndate(String signdate) {
		this.signdate = signdate;
	}

	public String getLnorganizeid() {
		return lnorganizeid;
	}

	public void setLnorganizeid(String lnorganizeid) {
		this.lnorganizeid = lnorganizeid;
	}

	@Override
	public String toString() {
		return "SignLocationEntity [pointno=" + pointno + ", name=" + name + ", pointlnggd=" + pointlnggd
				+ ", pointlatdg=" + pointlatdg + ", peopleno=" + peopleno + ", checktaskno=" + checktaskno
				+ ", signdate=" + signdate + ", lnorganizeid=" + lnorganizeid + "]";
	}

	
	
	
	
}
