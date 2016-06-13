package com.dsg.recogactivity.object;

public class Node {
	private float[] features; 
	private int type; // 1: Walk 2: Run
	private float distance;
	
	public Node(float[] features, int type) {
		this.features = features;
		this.type = type;
	}
	
	public float getDistance() {
		return distance;
	}
	public void setDistance(float distance) {
		this.distance = distance;
	}
	public float[] getFeatures() {
		return features;
	}
	public void setFeatures(float[] features) {
		this.features = features;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	} 
}
