package com.dsg.recogactivity.object;

public class ActInfo {
	private String action;
	private String time;
	
	public ActInfo() {
		
	}
	
	public ActInfo(String action, String time) {
		setAction(action);
		setTime(time);
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
