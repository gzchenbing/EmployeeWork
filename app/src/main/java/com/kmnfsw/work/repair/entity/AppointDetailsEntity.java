package com.kmnfsw.work.repair.entity;
/**
 * 任务详情实体
 * @author YanFaBu
 *
 */
public class AppointDetailsEntity {
	public String checkrepairno;//维修编号
    public int state;//维修状态
    public String releasedate;//任务派发时间
    public String exceptionpointid;//异常点ID
    public String extypename;//异常类型名
    public String exlocation;//异常位置
    public String exceptionlong;//异常点经度
    public String exceptionlat;//异常点纬度
    public String distributeType;//任务派发类型
    public String exceptionDescription;//异常类型描述
    public String pic;//图片地址
    public String voice;//语音路径
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
	public String getExceptionpointid() {
		return exceptionpointid;
	}
	public void setExceptionpointid(String exceptionpointid) {
		this.exceptionpointid = exceptionpointid;
	}
	public String getExtypename() {
		return extypename;
	}
	public void setExtypename(String extypename) {
		this.extypename = extypename;
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
	public String getDistributeType() {
		return distributeType;
	}
	public void setDistributeType(String distributeType) {
		this.distributeType = distributeType;
	}
	public String getExceptionDescription() {
		return exceptionDescription;
	}
	public void setExceptionDescription(String exceptionDescription) {
		this.exceptionDescription = exceptionDescription;
	}
	public String getPic() {
		return pic;
	}
	public void setPic(String pic) {
		this.pic = pic;
	}
	public String getVoice() {
		return voice;
	}
	public void setVoice(String voice) {
		this.voice = voice;
	}
	public AppointDetailsEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "AppointDetailsEntity [checkrepairno=" + checkrepairno + ", state=" + state + ", releasedate="
				+ releasedate + ", exceptionpointid=" + exceptionpointid + ", extypename=" + extypename
				+ ", exlocation=" + exlocation + ", exceptionlong=" + exceptionlong + ", exceptionlat=" + exceptionlat
				+ ", distributeType=" + distributeType + ", exceptionDescription=" + exceptionDescription + ", pic="
				+ pic + ", voice=" + voice + "]";
	}

    
}
