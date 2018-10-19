package com.kmnfsw.work.sign.entity;

import java.io.Serializable;

/**用于初始化任务*/
public class CheckTaskEntity implements Serializable{
	
	public String checkTaskNo;
	public String lineRoadList;
	public int peopleType;
	public String checkPlanNo;
	
	public CheckTaskEntity(String checkTaskNo, String lineRoadList, int peopleType, String checkPlanNo) {
		super();
		this.checkTaskNo = checkTaskNo;
		this.lineRoadList = lineRoadList;
		this.peopleType = peopleType;
		this.checkPlanNo = checkPlanNo;
	}

	public CheckTaskEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getCheckTaskNo() {
		return checkTaskNo;
	}

	public void setCheckTaskNo(String checkTaskNo) {
		this.checkTaskNo = checkTaskNo;
	}

	public String getLineRoadList() {
		return lineRoadList;
	}

	public void setLineRoadList(String lineRoadList) {
		this.lineRoadList = lineRoadList;
	}

	public int getPeopleType() {
		return peopleType;
	}

	public void setPeopleType(int peopleType) {
		this.peopleType = peopleType;
	}

	public String getCheckPlanNo() {
		return checkPlanNo;
	}

	public void setCheckPlanNo(String checkPlanNo) {
		this.checkPlanNo = checkPlanNo;
	}

	@Override
	public String toString() {
		return "CheckTaskEntity [checkTaskNo=" + checkTaskNo + ", lineRoadList=" + lineRoadList + ", peopleType="
				+ peopleType + ", checkPlanNo=" + checkPlanNo + "]";
	}
	

}
