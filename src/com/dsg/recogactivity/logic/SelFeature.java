package com.dsg.recogactivity.logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.os.Environment;
import android.util.Log;

import com.dsg.recogactivity.service.TrainService;

public class SelFeature {
	private List<float[]> list = new LinkedList<float[]>();

	private float[][] featureDifference;
	private float[][] avgFeature;

	private int[] selectFesture = new int[15];

	private String selFecture;

	private int num = 6;
	private int compareNum;
	
	private String path = Environment.getExternalStorageDirectory().getPath()
			+ "/TrainData/";
	
	private List<Integer> actCount = new LinkedList<Integer>();
	
	private List<float[]> sitList = new LinkedList<float[]>();
	private List<float[]> standList = new LinkedList<float[]>();
	private List<float[]> walkList = new LinkedList<float[]>();
	private List<float[]> descendStairList = new LinkedList<float[]>();
	private List<float[]> ascendStairList = new LinkedList<float[]>();
	private List<float[]> runList = new LinkedList<float[]>();

	public String getSelectFesture(String path) {
		list.clear();
		actCount.clear();
		
		readFile(path);

		avgFecture();
		calFestureDifference();
		normalization();
		selectFecture();

		selFecture = "";

		for (int i = 0; i < 15; i++)
			if (selectFesture[i] < 42)
				selFecture = selFecture + String.valueOf(selectFesture[i])
						+ ",";

		selFecture = selFecture.substring(0, selFecture.length() - 1);

		return selFecture;
	}

	// Ū���V�m���
	public void readFile(String path) {
		float[][] sit = new float[TrainService.LENGTH][42];
		float[][] stand = new float[TrainService.LENGTH][42];
		float[][] walk = new float[TrainService.LENGTH][42];
		float[][] descend = new float[TrainService.LENGTH][42];
		float[][] ascend = new float[TrainService.LENGTH][42];
		float[][] run = new float[TrainService.LENGTH][42];

		FileReader fr = null;
		String[] temp = new String[42];
		int lineCount = 0;
		int i = 0;

		try {
			fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();

			while (line != null) {
				if (line.length() > 0) {
					temp = line.split(",");

					switch (lineCount) {
					case 0:
						for (int j = 0; j < 42; j++)
							sit[i][j] = Float.valueOf(temp[j]);
						sitList.add(sit[i]);
						i++;
						break;
					case 1:
						for (int j = 0; j < 42; j++)
							stand[i][j] = Float.valueOf(temp[j]);
						standList.add(stand[i]);
						i++;
						break;
					case 2:
						for (int j = 0; j < 42; j++)
							walk[i][j] = Float.valueOf(temp[j]);
						walkList.add(walk[i]);
						i++;
						break;
					case 3:
						for (int j = 0; j < 42; j++)
							descend[i][j] = Float.valueOf(temp[j]);
						descendStairList.add(descend[i]);
						i++;
						break;
					case 4:
						for (int j = 0; j < 42; j++)
							ascend[i][j] = Float.valueOf(temp[j]);
						ascendStairList.add(ascend[i]);
						i++;
						break;
					case 5:
						for (int j = 0; j < 42; j++)
							run[i][j] = Float.valueOf(temp[j]);
						runList.add(run[i]);
						i++;
						break;
					default:
						break;
					}
				} else {
					lineCount++;
					i = 0;
				}

				line = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// �U��������
	public void avgFecture() {
		avgFeature = new float[num][42];
		float sum = 0;

		for (int i = 0; i < 42; i++) {
			sum = 0;		
			for (int j = 0; j < sitList.size(); j++) {
				sum += sitList.get(j)[i];
			}
			avgFeature[0][i] = sum / sitList.size();
		}
		
		for (int i = 0; i < 42; i++) {
			sum = 0;		
			for (int j = 0; j < standList.size(); j++) {
				sum += standList.get(j)[i];
			}
			avgFeature[1][i] = sum / standList.size();
		}
		
		for (int i = 0; i < 42; i++) {
			sum = 0;		
			for (int j = 0; j < walkList.size(); j++) {
				sum += walkList.get(j)[i];
			}
			avgFeature[2][i] = sum / walkList.size();
		}
		
		for (int i = 0; i < 42; i++) {
			sum = 0;		
			for (int j = 0; j < descendStairList.size(); j++) {
				sum += descendStairList.get(j)[i];
			}
			avgFeature[3][i] = sum / descendStairList.size();
		}
		
		for (int i = 0; i < 42; i++) {
			sum = 0;		
			for (int j = 0; j < ascendStairList.size(); j++) {
				sum += ascendStairList.get(j)[i];
			}
			avgFeature[4][i] = sum / ascendStairList.size();
		}
		
		for (int i = 0; i < 42; i++) {
			sum = 0;		
			for (int j = 0; j < runList.size(); j++) {
				sum += runList.get(j)[i];
			}
			avgFeature[5][i] = sum / runList.size();
		}
		
//		writeData();
	}

	// �p��S�x�Ȯt��
	public void calFestureDifference() {
		int index = 0;

		switch (num) {
		case 2:
			compareNum = 1;
			break;
		case 3:
			compareNum = 3;
			break;
		case 4:
			compareNum = 6;
			break;
		case 5:
			compareNum = 10;
			break;
		case 6:
			compareNum = 15;
			break;
		case 7:
			compareNum = 21;
			break;
		}

		featureDifference = new float[42][compareNum];

		for (int i = 0; i < num - 1; i++) {
			for (int j = i + 1; j < num; j++) {
				for (int k = 0; k < 42; k++) {
					featureDifference[k][index] = Math.abs(avgFeature[i][k]
							- avgFeature[j][k]);
				}

				index += 1;
			}
		}
		
//		writeData();
	}

	// �Y���0-1����
	public void normalization() {
		float[] temp = new float[42];
		float max, min;
		float normalValue;

		for (int i = 0; i < compareNum; i++) {
			for (int j = 0; j < 42; j++) {
				temp[j] = featureDifference[j][i];
			}

			Arrays.sort(temp);
			min = temp[0];
			max = temp[41];

			for (int j = 0; j < 42; j++) {
				normalValue = (featureDifference[j][i] - min) / (max - min);
				featureDifference[j][i] = normalValue;
			}
		}
	}

	// �D��S�x
	public void selectFecture() {
		float threshold = 0.9f;
		boolean isUseful;

		int[] isRecord = new int[compareNum];
		int[] count = new int[42];
		int[] isScan = new int[42]; // �O�_�w�D��

		int maxValue = 0, maxIndex = 0;
		int i, j, c;
		int index = 0;

		// ��l�@�ӵL�Ī��S�x�ȡA���Ĭ�0-41
		for (i = 0; i < 15; i++)
			selectFesture[i] = 42;

		do {
			isUseful = isUsefulThreshold(threshold);

			threshold -= 0.1f;
			
			System.out.println(threshold);

			if (threshold == 0)
				return;
		} while (!isUseful);

		threshold = threshold + 0.1f; // ��h�l�����[�^��
		
		// �D��̦h�w�W�L���e�Ȫ��S�x��
		for (i = 0; i < 42; i++) {
			for (j = 0; j < compareNum; j++) {
				if (featureDifference[i][j] >= threshold)
					count[i] += 1;
			}
		}

		// ���o�S�x�Ȧ�m
		for (i = 0; i < 42; i++) {
			if (count[i] > maxValue) {
				maxValue = count[i];
				maxIndex = i;
			}
		}

		selectFesture[index] = maxIndex;
		isScan[maxIndex] = 1;

		// �����ҹ����ϧO�S�x�Ȫ���m
		for (i = 0; i < compareNum; i++) {
			if (featureDifference[maxIndex][i] >= threshold) {
				isRecord[i] = 1;
			}
		}

		// // �Ĥ@�ؤ�k:�ʤ���N�ɤ���(�D�̨θ�)
		// for (i = 0; i < 15; i++) {
		// if (isRecord[i] == 0) {
		// for (j = 0; j < 42; j++) {
		//
		// if (FecutreDifference[j][i] >= threshold) {
		// isRecord[i] = 1;
		//
		// index += 1;
		//
		// selectFesture[index] = j;
		// break;
		// }
		// }
		// }
		// }

		// �ĤG�ؤ�k:���o�̨θ�
		while (true) {
			for (i = 0; i < 42; i++)
				count[i] = 0;

			c = 0;
			for (i = 0; i < compareNum; i++) {
				if (isRecord[i] == 1) {
					c += 1;
				}
			}

			if (c == compareNum) {
				break;
			} else {
				for (i = 0; i < 42; i++) {
					if (isScan[i] == 1) {
						continue;
					}
					for (j = 0; j < compareNum; j++) {
						if (isRecord[j] == 0) {
							if (featureDifference[i][j] >= threshold) {
								count[i] += 1;
							}
						}
					}
				}

				maxValue = 0;
				// ���o�S�x�Ȧ�m
				for (i = 0; i < 42; i++) {
					if (count[i] > maxValue) {
						maxValue = count[i];
						maxIndex = i;
					}
				}

				for (i = 0; i < compareNum; i++) {
					if (featureDifference[maxIndex][i] >= threshold) {
						isRecord[i] = 1;
					}
				}

				index++;
				selectFesture[index] = maxIndex;
			}
		}
	}

	// �P�_�O�_�O���Ϊ����e��
	private boolean isUsefulThreshold(float threshold) {
		int i, j;
		int[] isRecord = new int[compareNum];

		for (i = 0; i < 42; i++) {
			for (j = 0; j < compareNum; j++) {
				if (featureDifference[i][j] >= threshold)
					isRecord[j] += 1;
			}
		}

		for (i = 0; i < compareNum; i++) {
			if (isRecord[i] == 0)
				return false;
		}

		
		return true;
	}

	public String getSelFecture() {
		return selFecture;
	}
	
	public void writeData() {	
		try {
			FileWriter fw = new FileWriter(path + "/features.csv",
					false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < 42; i++) {
				for (int j = 0; j < compareNum; j++) {
					bw.write(String.valueOf(featureDifference[i][j]));
					if (j < 41)
						bw.write(",");
				}

				bw.newLine();
			}

			bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
