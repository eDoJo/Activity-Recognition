package com.dsg.recogactivity.logic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.dsg.recogactivity.object.Node;
import com.dsg.recogactivity.service.TrainService;

public class KNN {
	private List<Node> distNodeList = new LinkedList<Node>();
	private List<Node> sortNodeList = new LinkedList<Node>();

	private float[] tmp;

	public KNN() {

	}

	public void calDistance(List<Node> list, float[] f) {		
		List<Node> _list = new LinkedList<Node>();
				
		float distance;
		float[] temp1;
		float[] temp2;
		float sum;

		int num = list.size() / TrainService.LENGTH;
		float[][] center = new float[num][42];		

		for (int i = 0; i < num; i++) {
			for (int j = 0; j < 42; j++) {
				sum = 0;
				for (int k = 0; k < list.size(); k++) {
					sum = sum
							+ list.get(i * TrainService.LENGTH + k)
									.getFeatures()[j];
				}

				center[i][j] = sum / list.size();
			}

			_list.add(new Node(center[i], list.get(i * TrainService.LENGTH)
					.getType()));
		}

		for (int i = 0; i < _list.size(); i++) {
			distance = 0;
			temp1 = _list.get(i).getFeatures();
			temp2 = f;

			for (int j = 0; j < 42; j++) {
				distance = (float) (distance + Math.pow(temp1[j] - temp2[j], 2));
			}

			distance = (float) Math.sqrt(distance);
			_list.get(i).setDistance(distance);
			distNodeList.add(_list.get(i));
		}
	}

	public int rank() {
		tmp = new float[distNodeList.size()];

		for (int i = 0; i < distNodeList.size(); i++) {
			tmp[i] = distNodeList.get(i).getDistance();
		}

		Arrays.sort(tmp);

		for (int j = 0; j < distNodeList.size(); j++) {
			if (tmp[0] == distNodeList.get(j).getDistance()) {
				sortNodeList.add(distNodeList.get(j));

				break;
			}
		}

		distNodeList.clear();

		return sortNodeList.get(0).getType();
	}

	public void clearList() {
		distNodeList.clear();
		sortNodeList.clear();
	}
}
