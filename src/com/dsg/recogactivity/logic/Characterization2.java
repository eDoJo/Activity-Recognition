package com.dsg.recogactivity.logic;


public class Characterization2 {
	public static int size = 80;
	
	public static float[] convertToFeature(float[] x, float[] y, float[] z,
			int ch) {
				
		float[] features = new float[size];
		
		switch (ch) {
		case 1:
			for (int i = 0; i < size; i++) {
				features[i] = (float) (Math.pow(x[i], 2) + Math.pow(y[i], 2) + Math
						.pow(z[i], 2));
			}

			break;
		case 2:
			for (int i = 0; i < size; i++) {
				features[i] = (float) Math.sqrt(Math.pow(x[i], 2)
						+ Math.pow(z[i], 2) * 50 * 57.29578);
			}

			break;
		}		

		return dataFilter(features);
		
//		return features;
	}

	// Moving average filter
	public static float[] dataFilter(float[] features) {
		
		float[] filtFeatures = new float[size];
		float[] tmp = new float[size + 4];
		
		float sum = 0;
		
		tmp[0] = features[1];
		tmp[1] = features[2];	
		
		for (int i = 2; i < size + 2; i++) {
			tmp[i] = features[i - 2];
		}
		
		tmp[size + 2] = features[size - 2];
		tmp[size + 3] = features[size - 1];
		
		for (int j = 2; j < size + 2; j++) {
			sum = 0;
			
			for (int k = -2; k < 3; k++) {
				sum += tmp[j + k];
			}
			
			filtFeatures[j - 2] = sum / 5;
		}
		
		return filtFeatures;
	}
	
	public static float[] dataFilter2(float[] features) {
		int size = 2;
		
		float[] filtFeatures = new float[features.length];		
//		float[] tmp = new float[size];
		float sum = 0;
		
		for(int i = 0; i < features.length; i++) {
			sum = 0;
			for(int j = 0; j < size; j++) {
				if(i - j >= 0)
					sum += features[i - j];
			}
			
			filtFeatures[i] = sum / size;
		}
		
		return filtFeatures;
	}
}
