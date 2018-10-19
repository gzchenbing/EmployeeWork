package com.kmnfsw.work.sign.entity;

import java.io.Serializable;

public class TaskContentEntity implements  Serializable{
	
	public String peopleno;
	public String startTaskDate;
	public String endTaskDate;
	
	public String taskType;
	public String linesName;
	public String taskCycl;
	public TaskContentEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getPeopleno() {
		return peopleno;
	}
	public void setPeopleno(String peopleno) {
		this.peopleno = peopleno;
	}
	public String getStartTaskDate() {
		return startTaskDate;
	}
	public void setStartTaskDate(String startTaskDate) {
		this.startTaskDate = startTaskDate;
	}
	public String getEndTaskDatet() {
		return endTaskDate;
	}
	public void setEndTaskDatet(String endTaskDate) {
		this.endTaskDate = endTaskDate;
	}
	public String getPeopleType() {
		return taskType;
	}
	public void setPeopleType(String taskType) {
		this.taskType = taskType;
	}
	public String getLinesName() {
		return linesName;
	}
	public void setLinesName(String linesName) {
		this.linesName = linesName;
	}
	public String getTaskCycl() {
		return taskCycl;
	}
	public void setTaskCycl(String taskCycl) {
		this.taskCycl = taskCycl;
	}
	@Override
	public String toString() {
		return "TaskContentEntity [peopleno=" + peopleno + ", startTaskDate=" + startTaskDate + ", endTaskDate="
				+ endTaskDate + ", taskType=" + taskType + ", linesName=" + linesName + ", taskCycl=" + taskCycl
				+ "]";
	}
	
	

}
