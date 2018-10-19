package com.kmnfsw.work.sign.db.dao;

import java.util.ArrayList;
import java.util.List;

import com.kmnfsw.work.sign.db.TaskPointOpenHelper;
import com.kmnfsw.work.sign.entity.TaskPointEntity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class CheckedPointDao {
	private TaskPointOpenHelper taskPointOpenHelper;
	public CheckedPointDao(Context context) {
		super();
		taskPointOpenHelper = new TaskPointOpenHelper(context);
	}
	
	/**
	 * 添加CheckedPoint数据
	 * @param taskPointEntity
	 * @return
	 */
	public synchronized boolean addCheckedPoint(TaskPointEntity taskPointEntity){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id", taskPointEntity.id);
		values.put("localhostLong", taskPointEntity.localhostLong);
		values.put("localhostLat", taskPointEntity.localhostLat);
		values.put("lineId", taskPointEntity.lineId);
		values.put("lineName", taskPointEntity.lineName);
		long rowid = db.insert("checkedPoint", null, values);
		if (rowid == -1){ // 插入数据不成功
			db.close();
			return false;
		}else{
			db.close();
			return true;
		}
	}
	/**
	 * 根据巡检过点id删除巡检点
	 * @param id
	 * @return
	 */
	public synchronized boolean deleteCheckedPointById(String id){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		int rownumber = db.delete("checkedPoint", "id=?",
				new String[] { id });
		if (rownumber == 0){
			db.close();
			return false; // 删除数据不成功
		}else{
			db.close();
			return true;
		}
	}
	
	/**删除checkedPoint表里的所有数据*/
	public boolean deleteCheckedPoint(){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		try {
			db.execSQL("delete from checkedPoint");
			db.close();
			return true;
		} catch (SQLException e) {
			db.close();
			return false;
		}
		
	}
	
	/**
	 * 获取所有巡检过的点数据
	 * @return
	 */
	public List<TaskPointEntity> getAllCheckedPoint(){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("checkedPoint", new String[]{"id","localhostLong","localhostLat","lineId","lineName"}
			, null, null, null, null, null);
		List<TaskPointEntity> listTaskPoint = new ArrayList<>(); 
		while (cursor.moveToNext()) {
			TaskPointEntity task = new TaskPointEntity();
			task.id = cursor.getString(cursor.getColumnIndex("id"));
			task.localhostLong = cursor.getDouble(cursor.getColumnIndex("localhostLong"));
			task.localhostLat = cursor.getDouble(cursor.getColumnIndex("localhostLat"));
			task.lineId = cursor.getString(cursor.getColumnIndex("lineId"));
			task.lineName = cursor.getString(cursor.getColumnIndex("lineName"));
			listTaskPoint.add(task);
		}
		cursor.close();
		db.close();
		//SystemClock.sleep(30);
		return listTaskPoint;
	}
	/**
	 * 根据坐标点id查询巡检过的点信息
	 * @param id
	 * @return
	 */
	public TaskPointEntity getCheckedPointById(String id){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("checkedPoint", new String[]{"id","localhostLong","localhostLat","lineId","lineName"}
			, "id=?", new String[]{id }, null, null, null);
		TaskPointEntity task = new TaskPointEntity();
		if (cursor.moveToNext()) {
			task.id = cursor.getString(cursor.getColumnIndex("id"));
			task.localhostLong = cursor.getDouble(cursor.getColumnIndex("localhostLong"));
			task.localhostLat = cursor.getDouble(cursor.getColumnIndex("localhostLat"));
			task.lineId = cursor.getString(cursor.getColumnIndex("lineId"));
			task.lineName = cursor.getString(cursor.getColumnIndex("lineName"));
		}
		cursor.close();
		db.close();
		return task;
		
	}
	
	/**
	 * 根据巡检过点id判断巡检过的点是否存在
	 * @return
	 */
	public boolean isCheckedPointExist(String id){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("checkedPoint", null
			, "id=?", new String[]{id }, null, null, null);
		if (cursor.moveToNext()) {
			cursor.close();
			db.close();
			return true;
		}
		db.close();
		return false;
	}
	
	/**
	 * 查询checkedPoint表中的所有条目数
	 * @return
	 */
	public int getCheckedPointAllNumber(){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select count(*) from checkedPoint", null);
		cursor.moveToNext();
		int count = cursor.getInt(0);
		cursor.close();
		db.close();
		return count;
		
	}

}
