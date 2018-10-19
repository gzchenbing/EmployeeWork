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
import android.os.SystemClock;

public class TaskPointDao {

	private TaskPointOpenHelper taskPointOpenHelper;
	public TaskPointDao(Context context) {
		super();
		taskPointOpenHelper = new TaskPointOpenHelper(context);
	}
	
	/**
	 * 添加taskPoint数据
	 * @param taskPointEntity
	 * @return
	 */
	public synchronized boolean addTaskPoint(TaskPointEntity taskPointEntity){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("id", taskPointEntity.id);
		values.put("localhostLong", taskPointEntity.localhostLong);
		values.put("localhostLat", taskPointEntity.localhostLat);
		values.put("lineId", taskPointEntity.lineId);
		values.put("lineName", taskPointEntity.lineName);
		long rowid = db.insert("signTaskPoint", null, values);
		if (rowid == -1){ // 插入数据不成功
			db.close();
			return false;
		}else{
			db.close();
			return true;
		}
	}
	/**
	 * 根据任务点id删除任务点
	 * @param id
	 * @return
	 */
	public synchronized boolean deleteTaskPointById(String id){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		int rownumber = db.delete("signTaskPoint", "id=?",
				new String[] { id });
		if (rownumber == 0){
			db.close();
			return false; // 删除数据不成功
		}else{
			db.close();
			return true;
		}
	}
	
	/**删除signTaskPoint表里的所有数据*/
	public boolean deleteTaskPoint(){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		try {
			db.execSQL("delete from signTaskPoint");
			db.close();
			return true;
		} catch (SQLException e) {
			db.close();
			return false;
		}
		
	}
	
	/**
	 * 获取所有任务点数据
	 * @return
	 */
	public List<TaskPointEntity> getAllTaskPoint(){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("signTaskPoint", new String[]{"id","localhostLong","localhostLat","lineId","lineName"}
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
	 * 根据坐标点id查询任务信息
	 * @param id
	 * @return
	 */
	public TaskPointEntity getTaskPointById(String id){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("signTaskPoint", new String[]{"id","localhostLong","localhostLat","lineId","lineName"}
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
	 * 根据巡检任务点id判断任务是否存在
	 * @return
	 */
	public boolean isTaskPointExist(String id){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("signTaskPoint", null
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
	 * 查询signTaskPoint表中的所有条目数
	 * @return
	 */
	public int getTaskPointAllNumber(){
		SQLiteDatabase db = taskPointOpenHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select count(*) from signTaskPoint", null);
		cursor.moveToNext();
		int count = cursor.getInt(0);
		cursor.close();
		db.close();
		return count;
		
	}

}
