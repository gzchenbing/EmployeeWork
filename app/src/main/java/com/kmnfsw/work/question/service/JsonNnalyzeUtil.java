package com.kmnfsw.work.question.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import com.kmnfsw.work.question.util.androidTree.Dept;
import com.kmnfsw.work.question.util.androidTree.Node;
import com.kmnfsw.work.question.util.androidTree.NodeHelper;

import android.util.Log;

/**
 * json解析工具类
 * @author YanFaBu
 *
 */
public class JsonNnalyzeUtil {
	private final static String Tag = ".question.service.JsonNnalyzeUtil";
	
	private static List<Node> listNode=new ArrayList<>();
	
	/**路线json解析*/
	public static LinkedList<Node> JsonLnOrganize(List<LinkedHashMap<String,Object>> listResult){
		loopOperate(listResult);
		
		
		LinkedList<Node> mLinkedList = new LinkedList<>();
		mLinkedList.addAll(NodeHelper.sortNodes(listNode));
		return mLinkedList;
		
	}
	
	private static void loopOperate(List<LinkedHashMap<String,Object>> listResult){
		for (LinkedHashMap<String, Object> linkedHashMap : listResult) {
			Dept dept = new Dept();
			dept.id = (String)linkedHashMap.get("id");
			dept.name = (String)linkedHashMap.get("title");
			dept.parentId = (String)linkedHashMap.get("upId");
			dept.rank = (String)linkedHashMap.get("rank");
			listNode.add(dept);
			
			List<LinkedHashMap<String,Object>> list = (List)linkedHashMap.get("children");
			if (list.size()>0) {//当children不为空时进行递归调用
				loopOperate(list);
			}
		}
	}
	
	

}
