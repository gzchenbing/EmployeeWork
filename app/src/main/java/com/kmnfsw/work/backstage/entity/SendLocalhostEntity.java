package com.kmnfsw.work.backstage.entity;

public class SendLocalhostEntity {
	// 纬度
	private double Lat;
	// 经度
	private double Long;
	//人员工号
	private String peopleno;
	private String name;
	//人员类型 例：维修工、巡视工、维护工
	private int peopleType;
	//定位时间
	private String timestr;
	//MAC地址
	private String Mac;
	
	public SendLocalhostEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SendLocalhostEntity(double lat, double l, String peopleno, String name, int peopleType, String timestr,
			String mac) {
		super();
		Lat = lat;
		Long = l;
		this.peopleno = peopleno;
		this.name = name;
		this.peopleType = peopleType;
		this.timestr = timestr;
		Mac = mac;
	}

	public double getLat() {
		return Lat;
	}

	public void setLat(double lat) {
		Lat = lat;
	}

	public double getLong() {
		return Long;
	}

	public void setLong(double l) {
		Long = l;
	}

	public String getPeopleno() {
		return peopleno;
	}

	public void setPeopleno(String peopleno) {
		this.peopleno = peopleno;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPeopleType() {
		return peopleType;
	}

	public void setPeopleType(int peopleType) {
		this.peopleType = peopleType;
	}

	public String getTimestr() {
		return timestr;
	}

	public void setTimestr(String timestr) {
		this.timestr = timestr;
	}

	public String getMac() {
		return Mac;
	}

	public void setMac(String mac) {
		Mac = mac;
	}

	@Override
	public String toString() {
		return "SendLocalhostEntity [Lat=" + Lat + ", Long=" + Long + ", peopleno=" + peopleno + ", name=" + name
				+ ", peopleType=" + peopleType + ", timestr=" + timestr + ", Mac=" + Mac + "]";
	}

	
	
	
}
