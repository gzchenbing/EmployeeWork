package com.kmnfsw.work.question.util.androidTree;

import java.util.ArrayList;
import java.util.List;

import com.kmnfsw.work.R;

/**
 * @description  节点抽象类（泛型T主要是考虑到ID和parentID有可能是int型也有可能是String型
 * 即这里可以传入Integer或者String，具体什么类型由子类指定
，因为这两种类型比较是否相等的方式不同：一个是用 “==”，一个是用  equals() 函数）
 */
public abstract class Node<T> {

    private int _level = -1;//当前节点的层级，初始值-1 后面会讲到
    private List<Node> _childrenList = new ArrayList<>();//所有的孩子节点
    private Node _parent;//父亲节点
    private int _icon;//图标资源ID
    private boolean isExpand = false;//当前状态是否展开


    public abstract T get_id();//得到当前节点ID
    public abstract T get_parentId();//得到当前节点的父ID
    public abstract String get_label();//要显示的内容
    public abstract String get_rank();//得到当前节点级别
    public abstract boolean parent(Node dest);//判断当前节点是否是dest的父亲节点
    public abstract boolean child(Node dest);//判断当前节点是否是dest的孩子节点


    public int get_level() {
        if (_level == -1){//如果是 -1 的话就递归获取
            //因为是树形结构，所以此处想要得到当前节点的层级
            //，必须递归调用，但是递归效率低下，如果每次都递归获取会严重影响性能，所以我们把第一次
            //得到的结果保存起来避免每次递归获取
            int level = _parent == null ? 1 : _parent.get_level()+1;
            _level = level;
            return _level;
        }
        return _level;
    }

    public void set_level(int _level) {
        this._level = _level;
    }

    public List<Node> get_childrenList() {
        return _childrenList;
    }

    public void set_childrenList(List<Node> _childrenList) {
        this._childrenList = _childrenList;
    }

    public Node get_parent() {
        return _parent;
    }

    public void set_parent(Node _parent) {
        this._parent = _parent;
    }

    public int get_icon() {
        return _icon;
    }

    public void set_icon(int _icon) {
        this._icon = _icon;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setIsExpand(boolean isExpand) {
        this.isExpand = isExpand;
        if (isExpand){//是否展开
            _icon = R.drawable.tree_on;
        }else{
            _icon = R.drawable.tree_off;
        }
    }

    public boolean isRoot(){
        return _parent == null;
    }

    public boolean isLeaf(){
        return _childrenList.size() <= 0;
    }
    
    @Override
    public String toString() {
    	return "{id="+get_id()+",parentId="+get_parentId()+",name="+get_label()+",rank="+get_rank()+"}";
    }

}