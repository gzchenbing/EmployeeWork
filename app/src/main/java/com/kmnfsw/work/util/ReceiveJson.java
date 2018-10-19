package com.kmnfsw.work.util;


public class ReceiveJson<T> {

	public int state;
	public String msg;
	
	public T data;

	public ReceiveJson(int state, String msg, T data) {
		super();
		this.state = state;
		this.msg = msg;
		this.data = data;
	}

	public ReceiveJson() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "ReceiveJson [state=" + state + ", msg=" + msg + ", data=" + data + "]";
	}
	
	
}
