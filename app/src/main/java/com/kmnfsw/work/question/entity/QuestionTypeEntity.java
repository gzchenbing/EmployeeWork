package com.kmnfsw.work.question.entity;
/**
 * 问题类型实体
 * @author YanFaBu
 *
 */
public class QuestionTypeEntity {

	public String extypeid;
	public String extypename;
	
	
	public QuestionTypeEntity() {
		super();
	}


	public QuestionTypeEntity(String extypeid, String extypename) {
		super();
		this.extypeid = extypeid;
		this.extypename = extypename;
	}


	public String getExtypeid() {
		return extypeid;
	}


	public void setExtypeid(String extypeid) {
		this.extypeid = extypeid;
	}


	public String getExtypename() {
		return extypename;
	}


	public void setExtypename(String extypename) {
		this.extypename = extypename;
	}


	@Override
	public String toString() {
		return "QuestionTypeEntity [extypeid=" + extypeid + ", extypename=" + extypename + "]";
	}
	
	
}
