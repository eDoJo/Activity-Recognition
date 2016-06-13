package com.dsg.recogactivity.logic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;

import com.dsg.recogactivity.object.Action;
import com.dsg.recogactivity.object.Feature;
import com.dsg.recogactivity.service.TestService;
import com.dsg.recogactivity.utils.ToolKits;

public class ActKNN {
	private Action node; // add list node
	private Action action;
		
	private Context mContext;
	
	private List<Action> distActList = new LinkedList<Action>();
	private List<Action> sortActList = new LinkedList<Action>();
	
	private List<Integer> selFeatureList = new LinkedList<Integer>();

	private float[] tmp;
	
	private int isUseLongDistCal = 1;

	public ActKNN(Context mContext) {
		this.mContext = mContext;
	}

	public void loadselFeature(String position) {
		String str = ToolKits.getString(mContext, position, null);
		String[] tmp = str.split(",");

		selFeatureList.clear();
		NodeProc.selFeatureList.clear();

		for (int i = 0; i < tmp.length; i++) {
			selFeatureList.add(Integer.valueOf(tmp[i]));
			NodeProc.selFeatureList.add(Integer.valueOf(tmp[i]));
		}
	}	
	
	public void setActNode(Action node) {
		this.node = node;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public void calDistance(List<Action> list, int ch) {
		float distance;
		float[] temp1;
		float[] temp2;

		for (int i = 0; i < list.size(); i++) {
			distance = 0;
			temp1 = list.get(i).getFeature().getFeatures();
			temp2 = action.getFeature().getFeatures();

			switch (ch) {
			case 0:
				for (int j = 0; j < 42; j++) {
					distance = (float) (distance + Math.pow(
							temp1[j] - temp2[j], 2));
				}

				break;
			case 1:
				for (int j = 0; j < selFeatureList.size(); j++) {
					distance = (float) (distance + Math.pow(
							temp1[selFeatureList.get(j)]
									- temp2[selFeatureList.get(j)], 2));
				}

				break;
			}

			distance = (float) Math.sqrt(distance);
			list.get(i).setDistance(distance);
			distActList.add(list.get(i));
		}
	}

	public String rank() {
		String act = "";

		tmp = new float[distActList.size()];

		for (int i = 0; i < distActList.size(); i++) {
			tmp[i] = distActList.get(i).getDistance();
		}

		Arrays.sort(tmp);

		for (int j = 0; j < distActList.size(); j++) {
			if (tmp[0] == distActList.get(j).getDistance()) {
				sortActList.add(distActList.get(j));
				action.setDistance(tmp[0]);
				break;
			}
		}

		if (TestService.isUseFeedback == 0) {
			action.setTag(sortActList.get(0).getTag());

			distActList.clear();
			sortActList.clear();

			return action.getTag();
		} else {
			action.setTag(sortActList.get(0).getTag());	
			
			if(action.getTag().equals(TestService.selAct)) {
				isUsefulNode(action, sortActList.get(0).getTag());
			}

			distActList.clear();
			sortActList.clear();

			return action.getTag();
		}
	}

//	public String finVote() {
//		String type = "Null";
//
//		if (actionList.get(0).getTag().equals(actionList.get(1).getTag())) {
//			type = actionList.get(0).getTag();
//			if (!(type.equals("Null")))
//				preAct = type;
//		}
//
//		actionList.remove(actionList.size() - 2);
//
//		return preAct;
//	}

//	public float getDistance(List<Action> sortActList, String act) {
//		float min = 99999;
//
//		for (int i = 0; i < sortActList.size(); i++) {
//			if (sortActList.get(i).getTag().equals(act)) {
//				if (min >= sortActList.get(i).getDistance()) {
//					min = sortActList.get(i).getDistance();
//				}
//			}
//		}
//
//		return min;
//	}

	public void isUsefulNode(Action mAction, String type) {
//		String act = "Null";

		if (type.equals("Sit")) {
			if (mAction.getDistance() <= NodeProc.getKNNThresholdDist(
					TestService.sitNorList, isUseLongDistCal)) {
//				act = "Sit";

				NodeProc.deleteActionNode(TestService.sitList, TestService.sitNorList, isUseLongDistCal);
				NodeProc.addActionNode(TestService.sitList, new Action(
						new Feature(node.getFeature().getFeatures()), "Sit"));
			} else {
//				act = "Null";
			}
		} else if (type.equals("Stand")) {
			if (mAction.getDistance() <= NodeProc.getKNNThresholdDist(
					TestService.standNorList, isUseLongDistCal)) {
//				act = "Stand";

				NodeProc.deleteActionNode(TestService.standList, TestService.standNorList, isUseLongDistCal);
				NodeProc.addActionNode(TestService.standList,
						new Action(new Feature(node.getFeature()
								.getFeatures()), "Stand"));
			} else {
//				act = "Null";
			}
		} else if (type.equals("Walk")) {
			if (mAction.getDistance() <= NodeProc.getKNNThresholdDist(
					TestService.walkNorList, isUseLongDistCal)) {
//				act = "Walk";

				NodeProc.deleteActionNode(TestService.walkList, TestService.walkNorList, isUseLongDistCal);
				NodeProc.addActionNode(TestService.walkList, new Action(
						new Feature(node.getFeature().getFeatures()), "Walk"));
			} else {
//				act = "Null";
			}
		} else if (type.equals("DescendStair")) {
			if (mAction.getDistance() <= NodeProc.getKNNThresholdDist(
					TestService.descendStairNorList, isUseLongDistCal)) {
//				act = "DescendStair";

				NodeProc.deleteActionNode(TestService.descendStairList, TestService.descendStairNorList, isUseLongDistCal);
				NodeProc.addActionNode(TestService.descendStairList,
						new Action(new Feature(node.getFeature()
								.getFeatures()), "DescendStair"));
			} else {
//				act = "Null";
			}
		} else if (type.equals("AscendStair")) {
			if (mAction.getDistance() <= NodeProc.getKNNThresholdDist(
					TestService.ascendStairNorList, isUseLongDistCal)) {
//				act = "AscendStair";

				NodeProc.deleteActionNode(TestService.ascendStairList, TestService.ascendStairNorList, isUseLongDistCal);
				NodeProc.addActionNode(TestService.ascendStairList, new Action(
						new Feature(node.getFeature().getFeatures()),
						"AscendStair"));
			} else {
//				act = "Null";
			}
		} else if (type.equals("Run")) {
			if (mAction.getDistance() <= NodeProc.getKNNThresholdDist(
					TestService.runNorList, isUseLongDistCal)) {
//				act = "Run";

				NodeProc.deleteActionNode(TestService.runList, TestService.runNorList, isUseLongDistCal);
				NodeProc.addActionNode(TestService.runList, new Action(
						new Feature(node.getFeature().getFeatures()), "Run"));
			} else {
//				act = "Null";
			}
		}

//		return act;
	}
}
