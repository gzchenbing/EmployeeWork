package com.kmnfsw.work.version.entity;


public class VersionEntity{
	/**服务器版本号*/
	public String versionname;
	/**版本描述*/
	public String description;
	/**apk下载地址*/
	public String apkurl;
	public VersionEntity(String versionname, String description, String apkurl) {
		super();
		this.versionname = versionname;
		this.description = description;
		this.apkurl = apkurl;
	}
	public VersionEntity() {
		super();
	}
	public String getVersionname() {
		return versionname;
	}
	public void setVersionname(String versionname) {
		this.versionname = versionname;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getApkurl() {
		return apkurl;
	}
	public void setApkurl(String apkurl) {
		this.apkurl = apkurl;
	}
	@Override
	public String toString() {
		return "VersionEntity [versionname=" + versionname + ", description=" + description + ", apkurl=" + apkurl
				+ "]";
	}
	
	
}
