package com.kmnfsw.work.question.util.androidTree;

/**
 * @description 部门类（继承Node），此处的泛型Integer是因为ID和parentID都为int
 * ，如果为String传入泛型String即可，如果传入String，记得修改<span style="font-family: Arial, Helvetica, sans-serif;">parent和child方法，因为比较相等的方式不同。</span>
 */
public class Dept extends Node<String>{

    public String id;//部门ID
    public String parentId;//父亲节点ID
    public String name;//部门名称
    public String rank;//级别标记

    public Dept() {
    }

    public Dept(String id, String parentId, String name,String rank) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.rank = rank;
    }

    /**
     * 此处返回节点ID
     * @return
     */
    @Override
    public String get_id() {
        return id;
    }

    /**
     * 此处返回父亲节点ID
     * @return
     */
    @Override
    public String get_parentId() {
        return parentId;
    }

    @Override
    public String get_label() {
        return name;
    }
    @Override
	public String get_rank() {
		return rank;
	}
    @Override
    public boolean parent(Node dest) {
        if (id.equals(""+dest.get_parentId())){
            return true;
        }
        return false;
    }

    @Override
    public boolean child(Node dest) {
        if (parentId.equals(""+dest.get_id())){
            return true;
        }
        return false;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getRank(){
    	return rank;
    }
    
    public void setRank(String rank){
    	this.rank = rank;
    }

	
}
