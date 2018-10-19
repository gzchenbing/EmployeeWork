package com.kmnfsw.work.question.entity;
/**
 * 问题实体
 * @author YanFaBu
 *
 */
public class QuestionEntity {

	/**异常点id*/
	public String exceptionpointid;
	/**异常点维修状态*/
	public int repairirState;
	/**上报异常点的人*/
	public String peopleno;
	/**异常类型id*/
	public String extypeid;
	/**异常类型名称*/
	public String exTypeName;
	/**路线id*/
	public String lnOrganizeId;
	/**路线级别*/
	public String lnOrganizeType;
	/**异常点地址*/
	public String exlocation;
	/**异常点经度*/
	public String exceptionlong;
	/**异常点纬度*/
	public String exceptionlat;
	/**异常上报时间*/
	public String reportdate;
	/**异常上报描述*/
	public String exceptiondescription;
	/**图片名*/
	public String pictureName;
	/**图片数据*/
	public String pictureData;
	/**语音名*/
	public String voiceName;
	/**语音数据*/
	public String voiceData;
	
	public QuestionEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getExceptionpointid() {
		return exceptionpointid;
	}
	public void setExceptionpointid(String exceptionpointid) {
		this.exceptionpointid = exceptionpointid;
	}
	public int getRepairirState() {
		return repairirState;
	}
	public void setRepairirState(int repairirState) {
		this.repairirState = repairirState;
	}
	public String getPeopleno() {
		return peopleno;
	}
	public void setPeopleno(String peopleno) {
		this.peopleno = peopleno;
	}
	public String getExtypeid() {
		return extypeid;
	}
	public void setExtypeid(String extypeid) {
		this.extypeid = extypeid;
	}
	public String getExTypeName() {
		return exTypeName;
	}
	public void setExTypeName(String exTypeName) {
		this.exTypeName = exTypeName;
	}
	public String getLnOrganizeId() {
		return lnOrganizeId;
	}
	public void setLnOrganizeId(String lnOrganizeId) {
		this.lnOrganizeId = lnOrganizeId;
	}
	public String getLnOrganizeType() {
		return lnOrganizeType;
	}
	public void setLnOrganizeType(String lnOrganizeType) {
		this.lnOrganizeType = lnOrganizeType;
	}
	public String getExlocation() {
		return exlocation;
	}
	public void setExlocation(String exlocation) {
		this.exlocation = exlocation;
	}
	public String getExceptionlong() {
		return exceptionlong;
	}
	public void setExceptionlong(String exceptionlong) {
		this.exceptionlong = exceptionlong;
	}
	public String getExceptionlat() {
		return exceptionlat;
	}
	public void setExceptionlat(String exceptionlat) {
		this.exceptionlat = exceptionlat;
	}
	public String getReportdate() {
		return reportdate;
	}
	public void setReportdate(String reportdate) {
		this.reportdate = reportdate;
	}
	public String getExceptiondescription() {
		return exceptiondescription;
	}
	public void setExceptiondescription(String exceptiondescription) {
		this.exceptiondescription = exceptiondescription;
	}
	public String getPictureName() {
		return pictureName;
	}
	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
	}
	public String getPictureData() {
		return pictureData;
	}
	public void setPictureData(String pictureData) {
		this.pictureData = pictureData;
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
	@Override
	public String toString() {
		return "QuestionEntity [exceptionpointid=" + exceptionpointid + ", repairirState=" + repairirState
				+ ", peopleno=" + peopleno + ", extypeid=" + extypeid + ", exTypeName=" + exTypeName + ", lnOrganizeId="
				+ lnOrganizeId + ", lnOrganizeType=" + lnOrganizeType + ", exlocation=" + exlocation
				+ ", exceptionlong=" + exceptionlong + ", exceptionlat=" + exceptionlat + ", reportdate=" + reportdate
				+ ", exceptiondescription=" + exceptiondescription + ", pictureName=" + pictureName + ", pictureData="
				+ pictureData + ", voiceName=" + voiceName + ", voiceData=" + voiceData + "]";
	}
	
	
}
