package com.kmnfsw.work.sign.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskPointOpenHelper extends SQLiteOpenHelper{

	//创建数据库
	public TaskPointOpenHelper(Context context) {
		super(context, "SignTaskPoint。db", null, 1);
		
	}

	//创建数据表
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table signTaskPoint (id VARCHAR(50) primary key NOT NULL, localhostLong DOUBLE, localhostLat DOUBLE,lineId VARCHAR(50),lineName VARCHAR(50))");
		db.execSQL("create table checkedPoint (id VARCHAR(50) primary key NOT NULL, localhostLong DOUBLE, localhostLat DOUBLE,lineId VARCHAR(50),lineName VARCHAR(50))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
