package com.dsg.recogactivity.logic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Characterization {
	public static int size = 80;
		
	public static float[] _max = new float[42];
	public static float[] _min = new float[42];
	
	// Moving average filter
	public static float[] dataFilter(float[] features) {
		float[] tmp = new float[size + 4];
		float[] filtFeatures = new float[size];

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
	
	public static float[] convertToFeature(float[] x, float[] y, float[] z) {					
		float[] features = new float[42];		
		float[] xyz = new float[size]; 
		float[] xy = new float[size]; 
		float xyzSum = 0, xySum = 0; 
		float xyzMean = 0, xyMean = 0; 
		float xyzStd = 0, xyStd = 0;
		float[] xyzTemp = new float[size];
		float[] xyTemp = new float[size];		
        
		for(int i = 0; i < size; i++) {			
			xyz[i] = (float)(Math.pow(x[i], 2) + Math.pow(y[i], 2) + Math.pow(z[i], 2));			
			xy[i] = (float)(Math.pow(x[i], 2) + Math.pow(z[i], 2));
		}
		
		for(int i = 0; i < size; i++) {
			xyzSum += xyz[i];
			xySum += xy[i];
		}
        
		// average
		features[0] = xyzMean = xyzSum / size;
		features[21] = xyMean = xySum / size;
		
		// standard deviation
		for(int i = 0; i < size; i++) {
			xyzStd += (xyz[i] - xyzMean) * (xyz[i] - xyzMean);
			xyStd += (xy[i] - xyMean) * (xy[i] - xyMean);
		}
		
		xyzStd /= size;
		xyStd /= size;
		features[1]  = xyzStd = (float)Math.sqrt(xyzStd);
		features[22]  = xyStd = (float)Math.sqrt(xyStd);
		
		// copy array
		for(int i = 0; i < size; i++) {
			xyzTemp[i] = xyz[i];
			xyTemp[i] = xy[i];
		}
		
		// sorted array by order
		Arrays.sort(xyzTemp);
		Arrays.sort(xyTemp);
		
		// min
		features[2] = xyzTemp[0];
		features[23] = xyTemp[0];
		
		// max
		features[3] = xyzTemp[size - 1];
		features[24] = xyTemp[size - 1];
		
		// percentile(0.1)
		features[4] = xyzTemp[(int) (size * 0.1 - 1)];
		features[25] = xyTemp[(int) (size * 0.1 - 1)];
		
		// percentile(0.25)
		features[5] = xyzTemp[(int) (size * 0.25 - 1)];
		features[26] = xyTemp[(int) (size * 0.25 - 1)];
		
		// percentile(0.5)
		features[6] = xyzTemp[(int) (size * 0.5 - 1)];
		features[27] = xyTemp[(int) (size * 0.5 - 1)];
				
		// percentile(0.75)
		features[7] = xyzTemp[(int) (size * 0.75 - 1)];
		features[28] = xyTemp[(int) (size * 0.75 - 1)];	 
				
		// percentile(0.9)
		features[8] = xyzTemp[(int) (size * 0.9 - 1)] ;
		features[29] = xyTemp[(int) (size * 0.9 - 1)];
		
		// sum of per(0.05)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.05; i++)
		{
			xyzSum += xyzTemp[i];
			xySum += xyTemp[i];
		}
		
		features[9] = xyzSum;
		features[30] = xySum;
				
		// sum of per(0.1)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.1; i++)
		{
			xyzSum += xyzTemp[i];
			xySum += xyTemp[i];
		}
		
		features[10] = xyzSum;
		features[31] = xySum;
				
        // sum of per(0.25)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.25; i++)
		{
			xyzSum += xyzTemp[i];
			xySum += xyTemp[i];
		}
		
		features[11] = xyzSum;
		features[32] = xySum;	
				
        // sum of per(0.75)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.75; i++)
		{
			xyzSum += xyzTemp[i];
			xySum += xyTemp[i];
		}
		
		features[12] = xyzSum;
		features[33] = xySum;
				
        // sum of per(0.9)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.9; i++)
		{
			xyzSum += xyzTemp[i];
			xySum += xyTemp[i];
		}
		
		features[13] = xyzSum;	 
		features[34] = xySum;
				 
        // sum of per(0.95)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.95; i++)
		{
			xyzSum += xyzTemp[i];
			xySum += xyTemp[i];
		}
		
		features[14] = xyzSum;		
		features[35] = xySum;	
		
		// square sum of per(0.05)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.05; i++)
		{
			xyzSum += (float)Math.pow(xyzTemp[i], 2);
			xySum += (float)Math.pow(xyTemp[i], 2);
		}
		
		features[15] = xyzSum;
		features[36] = xySum;	
				
        // square sum of per(0.1)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.1; i++)
		{
			xyzSum += (float)Math.pow(xyzTemp[i], 2);
			xySum += (float)Math.pow(xyTemp[i], 2);
		}
		
		features[16] = xyzSum;
		features[37] = xySum;
				
        // square sum of per(0.25)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.25; i++)
		{
			xyzSum += (float)Math.pow(xyzTemp[i], 2);
			xySum += (float)Math.pow(xyTemp[i], 2);
		}
		
		features[17] = xyzSum;	
		features[38] = xySum;	
				
		// square sum of per(0.75)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.75; i++)
		{
			xyzSum += (float)Math.pow(xyzTemp[i], 2);
			xySum += (float)Math.pow(xyTemp[i], 2);
		}
		
		features[18] = xyzSum;	
		features[39] = xySum;	
				
		// square sum of per(0.9)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.9; i++)
		{
			xyzSum += (float)Math.pow(xyzTemp[i], 2);
			xySum += (float)Math.pow(xyTemp[i], 2);
		}
		
		features[19] = xyzSum;
		features[40] = xySum;		
		
		// square sum of per(0.95)
		xyzSum = 0;
		xySum = 0;		
		for(int i = 0; i < size * 0.95; i++)
		{
			xyzSum += (float)Math.pow(xyzTemp[i], 2);
			xySum += (float)Math.pow(xyTemp[i], 2);
		}
		
		features[20] = xyzSum;
		features[41] = xySum;	
				
		return features;
	}
		
	public static float[] normalization(float[] features, float[] max, float[] min) {			
		for(int i = 0; i < 42; i++)
		{
			features[i] = (float)((features[i] - min[i]) / ((max[i]) - (min[i])));
		}
		
		return features;
	}
	
	public static List<float[]> normalization(List<float[]> list) {
		List<float[]> _list = new LinkedList<float[]>();
		
		float[] tmp = new float[list.size()];		
		float[][] feature = new float[list.size()][42];
		
		float[] min = new float[42];
		float[] max = new float[42];

		for (int i = 0; i < 42; i++) {
			for (int j = 0; j < list.size(); j++) {
				tmp[j] = list.get(j)[i];
			}
			
			Arrays.sort(tmp);
			
			min[i] = tmp[0];
			max[i] = tmp[list.size() - 1];	
			
			_min[i] = tmp[0];
			_max[i] = tmp[list.size() - 1];
		}
		
		for(int i = 0; i < list.size(); i++) {
			for(int j = 0; j < 42; j++) {
				feature[i][j] = (float)((list.get(i)[j] - min[j]) / ((max[j]) - (min[j])));
			}
			
			_list.add(feature[i]);
		}
							
		return _list;
	}
	
	public static float[] normalization(float[] tmp) {
		float[] _tmp = new float[42];
		
		for(int i = 0; i < 42; i++) {
			_tmp[i] = (float)((tmp[i] - _min[i]) / (_max[i] - _min[i]));
		}
		
		return _tmp;
	}
}
