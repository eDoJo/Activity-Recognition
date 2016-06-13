package com.dsg.recogactivity.logic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.dsg.recogactivity.object.Position;

public class PosKNN {
	private List<Position> posList = new LinkedList<Position>();
	private List<Position> distPosList = new LinkedList<Position>();
	private List<Position> sortPosList = new LinkedList<Position>();

	private float[] tmp;

	private Position position;

	public PosKNN() {

	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public void calDistance(List<Position> list, Position position) {
		float distance;

		float[] temp1;
		float[] temp2;

		for (int i = 0; i < list.size(); i++) {
			distance = 0;
			temp1 = list.get(i).getFeature().getFeatures();
			temp2 = position.getFeature().getFeatures();

			for (int j = 0; j < 42; j++) {
				distance = (float) (distance + Math.pow(temp1[j] - temp2[j], 2));
			}

			distance = (float) Math.sqrt(distance);
			list.get(i).setDistance(distance);
			distPosList.add(list.get(i));
		}
	}

	public void rank() {
		tmp = new float[distPosList.size()];

		for (int i = 0; i < distPosList.size(); i++) {
			tmp[i] = distPosList.get(i).getDistance();
		}

		Arrays.sort(tmp);

		for (int j = 0; j < distPosList.size(); j++) {
			if (tmp[0] == distPosList.get(j).getDistance()) {
				sortPosList.add(distPosList.get(j));
				break;
			}

		}

		position.setTag(sortPosList.get(0).getTag());

		distPosList.clear();
		sortPosList.clear();
		
		posList.add(position);
	}

	public String finVote() {
		String type = "Null";

		if (posList.get(0).getTag().equals(posList.get(1).getTag())) {
			type = posList.get(0).getTag();
		}

		posList.remove(posList.size() - 2);

		return type;
	}

//	public float getDistance(List<Position> sortPosList, String pos) {
//		float min = 99999;
//
//		for (int i = 0; i < sortPosList.size(); i++) {
//			if (sortPosList.get(i).getTag().equals(pos)) {
//				if (min >= sortPosList.get(i).getDistance()) {
//					min = sortPosList.get(i).getDistance();
//				}
//			}
//		}
//
//		return min;
//	}

//	public void isUsefulNode(Position mPosition, String type) {
//		if (type.equals("frontPocket")) {
//			if (mPosition.getDistance() <= NodeProc
//					.getKNNPosThresholdDist(TestService.frontPocket)) {
//				position.setTag("frontPocket");
//			} else {
//				position.setTag("Null");
//			}
//		} else if (type.equals("backPocket")) {
//			if (mPosition.getDistance() <= NodeProc
//					.getKNNPosThresholdDist(TestService.backPocket)) {
//				position.setTag("backPocket");
//			} else {
//				position.setTag("Null");
//			}
//		}
//
//		else if (type.equals("coatPocket")) {
//			if (mPosition.getDistance() <= NodeProc
//					.getKNNPosThresholdDist(TestService.coatPocket)) {
//				position.setTag("coatPocket");
//			} else {
//				position.setTag("Null");
//			}
//		} else if (type.equals("backBag")) {
//			if (mPosition.getDistance() <= NodeProc
//					.getKNNPosThresholdDist(TestService.backBag)) {
//				position.setTag("backBag");
//			} else {
//				position.setTag("Null");
//			}
//		} else if (type.equals("shoulderBag")) {
//			if (mPosition.getDistance() <= NodeProc
//					.getKNNPosThresholdDist(TestService.shoulderBag)) {
//				position.setTag("shoulderBag");
//			} else {
//				position.setTag("Null");
//			}
//		}
//	}
}
