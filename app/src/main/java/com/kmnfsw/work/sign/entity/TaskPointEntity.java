package com.kmnfsw.work.sign.entity;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class TaskPointEntity implements Parcelable {

	public String id;
	public double localhostLong;
	public double localhostLat;
	public String lineId;
	public String lineName;

	public TaskPointEntity() {
		super();
	}

	public TaskPointEntity(String id, double localhostLong, double localhostLat, String lineId, String lineName) {
		super();
		this.id = id;
		this.localhostLong = localhostLong;
		this.localhostLat = localhostLat;
		this.lineId = lineId;
		this.lineName = lineName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getLocalhostLong() {
		return localhostLong;
	}

	public void setLocalhostLong(double localhostLong) {
		this.localhostLong = localhostLong;
	}

	public double getLocalhostLat() {
		return localhostLat;
	}

	public void setLocalhostLat(double localhostLat) {
		this.localhostLat = localhostLat;
	}

	public String getLineId() {
		return lineId;
	}

	public void setLineId(String lineId) {
		this.lineId = lineId;
	}

	public String getLineName() {
		return lineName;
	}

	public void setLineName(String lineName) {
		this.lineName = lineName;
	}

	@Override
	public String toString() {
		return "TaskPointEntity [id=" + id + ", localhostLong=" + localhostLong + ", localhostLat=" + localhostLat
				+ ", lineId=" + lineId + ", lineName=" + lineName + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeDouble(localhostLong);
		dest.writeDouble(localhostLat);
		dest.writeString(lineId);
		dest.writeString(lineName);
	}

	public static final Parcelable.Creator<TaskPointEntity> CREATOR = new Parcelable.Creator<TaskPointEntity>() {

		@Override
		public TaskPointEntity createFromParcel(Parcel source) {
			TaskPointEntity taskPointEntity = new TaskPointEntity();
			taskPointEntity.id = source.readString();
			taskPointEntity.localhostLong = source.readDouble();
			taskPointEntity.localhostLat = source.readDouble();
			taskPointEntity.lineId = source.readString();
			taskPointEntity.lineName = source.readString();
			return taskPointEntity;
		}

		@Override
		public TaskPointEntity[] newArray(int size) {
			return new TaskPointEntity[size];
		}
	};

}
