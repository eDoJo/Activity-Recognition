package com.dsg.recogactivity.object;

public class Feature {
	private float[] features;
    
	public Feature(float[] features) {	
		this.features = features;
	}
	
	public float[] getFeatures() {
		return features;
	}

	public void setFeatures(float[] features) {
		this.features = features;
	}
}
