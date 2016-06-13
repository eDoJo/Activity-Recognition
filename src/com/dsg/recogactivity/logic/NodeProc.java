package com.dsg.recogactivity.logic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.dsg.recogactivity.object.Action;
import com.dsg.recogactivity.object.Position;

public class NodeProc {
	public static List<Integer> selFeatureList = new LinkedList<Integer>();

	public static void addActionNode(List<Action> list, Action action) {
		if (action.getTag().equals("Sit")) {
			list.add(action);
		} else if (action.getTag().equals("Stand")) {
			list.add(action);
		} else if (action.getTag().equals("Walk")) {
			list.add(action);
		} else if (action.getTag().equals("DescendStair")) {
			list.add(action);
		} else if (action.getTag().equals("AscendStair")) {
			list.add(action);
		} else if (action.getTag().equals("Run")) {
			list.add(action);
		}
	}

	public static void deleteActionNode(List<Action> list, List<float[]> norList, int ch) {
		int position = 0;

		switch (ch) {
		case 0:
			position = getShortDistPos(norList);
			break;
		case 1:
			position = getLongDistPos(norList);
			break;
		}

		list.remove(position);
	}

	public static int getShortDistPos(List<float[]> list) {
		float[] center = new float[42];
		float[] distance = new float[list.size()];
		float sum;
		int position = 0;

		for (int i = 0; i < 42; i++) {
			sum = 0;
			for (int j = 0; j < list.size(); j++) {
				sum = sum + list.get(j)[i];
			}

			center[i] = sum / list.size();
		}

		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < selFeatureList.size(); j++) {
				distance[i] = (float) (distance[i] + Math.pow(list.get(i)[selFeatureList.get(j)]
						- center[j], 2));
			}

			distance[i] = (float) Math.sqrt(distance[i]);
		}

		float min = distance[0];

		for (int i = 0; i < distance.length; i++) {
			if (distance[i] < min) {
				min = distance[i];
				position = i;
			}
		}

		return position;
	}

	public static int getLongDistPos(List<float[]> list) {
		float[] center = new float[42];
		float[] distance = new float[list.size()];
		float sum;
		int position = 0;

		for (int i = 0; i < 42; i++) {
			sum = 0;
			for (int j = 0; j < list.size(); j++) {
				sum = sum + list.get(j)[i];
			}

			center[i] = sum / list.size();
		}

		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < selFeatureList.size(); j++) {
				distance[i] = (float) (distance[i] + Math.pow(list.get(i)[selFeatureList.get(j)]
						- center[j], 2));
			}

			distance[i] = (float) Math.sqrt(distance[i]);
		}

		float max = distance[0];

		for (int i = 0; i < distance.length; i++) {
			if (distance[i] > max) {
				max = distance[i];
				position = i;
			}
		}

		return position;
	}

	/**
	 * find long distance with any two node in same activity group (new)
	 * 
	 * @param list
	 * @return
	 */
	public static float getKNNThresholdDist(List<float[]> list, int ch) {
		float[] center = new float[42];
		float[] distance = new float[list.size()];
		float sum;

		for (int i = 0; i < 42; i++) {
			sum = 0;
			for (int j = 0; j < list.size(); j++) {
				sum = sum + list.get(j)[i];
			}

			center[i] = sum / list.size();
		}

		for (int i = 0; i < list.size(); i++) {
			switch (ch) {
			case 0:
				for (int k = 0; k < 42; k++) {
					distance[i] = (float) (distance[i] + Math.pow(list.get(i)[k]
							- center[k], 2));
				}

				break;
			case 1:
				for (int k = 0; k < selFeatureList.size(); k++) {
					distance[i] = (float) (distance[i] + Math.pow(list.get(i)[selFeatureList.get(k)]
							- center[selFeatureList.get(k)], 2));
				}

				break;
			}

			distance[i] = (float) Math.sqrt(distance[i]);
		}

		Arrays.sort(distance);

		return distance[distance.length - 1];
	}

	public static float getKNNPosThresholdDist(List<Position> list) {
		float[] center = new float[42];
		float[] distance = new float[list.size()];
		float sum;

		for (int i = 0; i < 42; i++) {
			sum = 0;
			for (int j = 0; j < list.size(); j++) {
				sum = sum + list.get(j).getFeature().getFeatures()[i];
			}

			center[i] = sum / list.size();
		}

		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < 42; j++) {
				distance[i] = (float) (distance[i] + Math.pow(list.get(i)
						.getFeature().getFeatures()[j]
						- center[j], 2));
			}

			distance[i] = (float) Math.sqrt(distance[i]);
		}

		Arrays.sort(distance);

		return distance[distance.length - 1];
	}
}
