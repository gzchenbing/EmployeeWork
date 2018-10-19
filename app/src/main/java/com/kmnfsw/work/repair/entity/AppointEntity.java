package com.kmnfsw.work.repair.entity;
/**
 * 任务实体用于任务列表展示用
 * @author YanFaBu
 *
 */
public class AppointEntity {

	public String checkrepairno;
	public int state;
	public String releasedate;
	public String exLocation;
	public String extypename;
	public AppointEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getCheckrepairno() {
		return checkrepairno;
	}
	public void setCheckrepairno(String checkrepairno) {
		this.checkrepairno = checkrepairno;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String getReleasedate() {
		return releasedate;
	}
	public void setReleasedate(String releasedate) {
		this.releasedate = releasedate;
	}
	public String getExLocation() {
		return exLocation;
	}
	public void setExLocation(String exLocation) {
		this.exLocation = exLocation;
	}
	public String getExtypename() {
		return extypename;
	}
	public void setExtypename(String extypename) {
		this.extypename = extypename;
	}
	@Override
	public String toString() {
		return "AppointEntity [checkrepairno=" + checkrepairno + ", state=" + state + ", releasedate=" + releasedate
				+ ", exLocation=" + exLocation + ", extypename=" + extypename + "]";
	}
	
	
}
