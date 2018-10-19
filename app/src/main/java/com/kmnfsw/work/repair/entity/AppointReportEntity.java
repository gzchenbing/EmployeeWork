package com.kmnfsw.work.repair.entity;

import java.util.List;

public class AppointReportEntity {

	public String checkrepairno;
	public String peopleno;
	public String reportDate;
	public String userMaterial;
	public String depict;
	public String voiceName;
	public String voiceData;
	public List<Picture> listPicture;
	public AppointReportEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	public AppointReportEntity(String checkrepairno, String peopleno, String reportDate, String userMaterial,
			String depict, String voiceName, String voiceData, List<Picture> listPicture) {
		super();
		this.checkrepairno = checkrepairno;
		this.peopleno = peopleno;
		this.reportDate = reportDate;
		this.userMaterial = userMaterial;
		this.depict = depict;
		this.voiceName = voiceName;
		this.voiceData = voiceData;
		this.listPicture = listPicture;
	}
	public String getCheckrepairno() {
		return checkrepairno;
	}
	public void setCheckrepairno(String checkrepairno) {
		this.checkrepairno = checkrepairno;
	}
	public String getPeopleno() {
		return peopleno;
	}
	public void setPeopleno(String peopleno) {
		this.peopleno = peopleno;
	}
	public String getReportDate() {
		return reportDate;
	}
	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}
	public String getUserMaterial() {
		return userMaterial;
	}
	public void setUserMaterial(String userMaterial) {
		this.userMaterial = userMaterial;
	}
	public String getDepict() {
		return depict;
	}
	public void setDepict(String depict) {
		this.depict = depict;
	}
	public String getVoiceName() {
		return voiceName;
	}
	public void setVoiceName(String voiceName) {
		this.voiceName = voiceName;
	}
	public String getVoiceData() {
		return voiceData;
	}
	public void setVoiceData(String voiceData) {
		this.voiceData = voiceData;
	}
	public List<Picture> getListPicture() {
		return listPicture;
	}
	public void setListPicture(List<Picture> listPicture) {
		this.listPicture = listPicture;
	}
	@Override
	public String toString() {
		return "AppointReportEntity [checkrepairno=" + checkrepairno + ", peopleno=" + peopleno + ", reportDate="
				+ reportDate + ", userMaterial=" + userMaterial + ", depict=" + depict + ", voiceName=" + voiceName
				+ ", voiceData=" + voiceData + ", listPicture=" + listPicture + "]";
	}
	
	
}
