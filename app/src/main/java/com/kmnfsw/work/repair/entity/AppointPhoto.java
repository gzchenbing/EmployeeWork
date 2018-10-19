package com.kmnfsw.work.repair.entity;

import android.graphics.Bitmap;

public class AppointPhoto {

	public String photo_path;
	public Bitmap new_bitmap;
	public AppointPhoto(String photo_path, Bitmap new_bitmap) {
		super();
		this.photo_path = photo_path;
		this.new_bitmap = new_bitmap;
	}
	
	
	
}
