package com.dsg.recogactivity.object;

public class Action {
	private Feature feature;
	private String tag = null;
	private float distance = 0;
	
	public Action() {
		
	}
	
	public Action(Feature feature, String tag) {		
		this.feature = feature;
		this.tag = tag;
	}

	public Feature getFeature() {
		return feature;
	}

	public void setFeatures(Feature features) {
		this.feature = features;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}	
}
