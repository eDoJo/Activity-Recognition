package com.dsg.recogactivity.logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.dsg.recogactivity.object.Action;
import com.dsg.recogactivity.object.Feature;
import com.dsg.recogactivity.service.TrainService;

import android.os.Environment;
import android.util.Log;

public class GenerateData {
	private String path = Environment.getExternalStorageDirectory().getPath()
			+ "/TrainData/";

	private String[] curPos;

	private List<float[]> sitList = new LinkedList<float[]>();
	private List<float[]> standList = new LinkedList<float[]>();
	private List<float[]> walkList = new LinkedList<float[]>();
	private List<float[]> descendStairList = new LinkedList<float[]>();
	private List<float[]> ascendStairList = new LinkedList<float[]>();
	private List<float[]> runList = new LinkedList<float[]>();

	public void generData(String[] pos) {
		curPos = pos;
		
		isExitFolder(path + "TrainData");

		for (int i = 0; i < curPos.length; i++) {
			clearList();
			
			isExitFolder(path + "TrainData" + "/" + "_" + curPos[i]);
			
			loadFeatureData(path + curPos[i] + "/data_act.csv");
			
//			Log.d("logger", String.valueOf(descendStairList.size()));
			
			for(int j = 2; j < 10; j++) {
//				Log.d("logger", String.valueOf(j - 5));
				descendStairList.remove(2);
				ascendStairList.remove(2);
//				Log.d("logger", String.valueOf(descendStairList.size()));
			}
			
			add_Des_Asc_Data(curPos[i]);
			
			writeFeatureData("_" + curPos[i]);
			
			writeNorFeatureData("_" + curPos[i]);
		}
	}

	public void clearList() {
		sitList.clear();
		standList.clear();
		walkList.clear();
		descendStairList.clear();
		ascendStairList.clear();
		runList.clear();
	}

	public void loadFeatureData(String path) {
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

	public void loadAccData(String path, List<float[]> list) {
		float[] x = new float[Characterization.size];
		float[] y = new float[Characterization.size];
		float[] z = new float[Characterization.size];
		int i = 0;

		FileReader fr = null;
		String[] temp = new String[3];

		try {
			fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();

			while (line != null) {
				if (line.length() > 0) {
					temp = line.split(",");

					x[i] = Float.valueOf(temp[0]);
					y[i] = Float.valueOf(temp[1]);
					z[i] = Float.valueOf(temp[2]);

					i++;
				}

				line = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		convFeatureData(x, y, z, list);
	}

	public void convFeatureData(float[] x1, float[] y1, float[] z1,
			List<float[]> list) {
		float[] x = new float[Characterization.size];
		float[] y = new float[Characterization.size];
		float[] z = new float[Characterization.size];

		float[] _x = new float[Characterization.size];
		float[] _y = new float[Characterization.size];
		float[] _z = new float[Characterization.size];

		for (int i = 0; i < Characterization.size; i++) {
			x[i] = x1[i];
			y[i] = y1[i];
			z[i] = z1[i];
		}

		_x = Characterization.dataFilter(x);
		_y = Characterization.dataFilter(y);
		_z = Characterization.dataFilter(z);

		list.add(Characterization.convertToFeature(_x, _y, _z));
	}

	public void add_Des_Asc_Data(String pos) {
		for (int i = 0; i < 4; i++) {
			loadAccData(path + "feedback" + "/" + pos + "/DescendStair"
					+ "/Acc" + "/" + String.valueOf(i) + ".csv",
					descendStairList);

			loadAccData(path + "feedback" + "/" + pos + "/AscendStair"
					+ "/Acc" + "/" + String.valueOf(i) + ".csv",
					ascendStairList);
		}
	}

	public void isExitFolder(String s) {
		File file = new File(s);

		if (!file.exists()) {
			file.mkdir();
		}
	}

	public void writeFeatureData(String pos) {
		try {
			FileWriter fw = new FileWriter(path + "TrainData" + "/" + pos
					+ "/data_act.csv", false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < sitList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(sitList.get(i)[j]));

					if (j < 41) {
						bw.write(",");
					}
				}

				bw.newLine();
			}

			bw.newLine();

			for (int i = 0; i < standList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(standList.get(i)[j]));

					if (j < 41) {
						bw.write(",");
					}
				}

				bw.newLine();
			}

			bw.newLine();

			for (int i = 0; i < walkList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(walkList.get(i)[j]));

					if (j < 41) {
						bw.write(",");
					}
				}

				bw.newLine();
			}

			bw.newLine();

			for (int i = 0; i < descendStairList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(descendStairList.get(i)[j]));

					if (j < 41) {
						bw.write(",");
					}
				}

				bw.newLine();
			}

			bw.newLine();

			for (int i = 0; i < ascendStairList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(ascendStairList.get(i)[j]));

					if (j < 41) {
						bw.write(",");
					}
				}

				bw.newLine();
			}

			bw.newLine();

			for (int i = 0; i < runList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(runList.get(i)[j]));

					if (j < 41) {
						bw.write(",");
					}
				}

				bw.newLine();
			}

			bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public List<float[]> convNorFeatureList() {
		List<float[]> list = new LinkedList<float[]>();
		
		for(int i = 0; i < sitList.size(); i++) {
			list.add(sitList.get(i));
		}
		
		for(int i = 0; i < standList.size(); i++) {
			list.add(standList.get(i));
		}
		
		for(int i = 0; i < walkList.size(); i++) {
			list.add(walkList.get(i));
		}
		
		for(int i = 0; i < descendStairList.size(); i++) {
			list.add(descendStairList.get(i));
		}
		
		for(int i = 0; i < ascendStairList.size(); i++) {
			list.add(ascendStairList.get(i));
		}
		
		for(int i = 0; i < runList.size(); i++) {
			list.add(runList.get(i));
		}
		
		list = Characterization.normalization(list);
		
		return list;
	}
	
	public void writeNorFeatureData(String pos) {
		List<float[]> list = new LinkedList<float[]>();
		int index = 0;

		list = convNorFeatureList();
		
		try {
			FileWriter fw = new FileWriter(path + "TrainData" + "/" + pos
					+ "/data_act_normal.csv", false);
			BufferedWriter bw = new BufferedWriter(fw);
			
			
			for(int i = index; i < index + sitList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(list.get(i)[j]));
					if (j < 41)
						bw.write(",");
				}
				
				bw.newLine();
			}
			
			bw.newLine();
			index += sitList.size();
			
			for(int i = index; i < index + standList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(list.get(i)[j]));
					if (j < 41)
						bw.write(",");
				}
				
				bw.newLine();
			}
						
			bw.newLine();
			index += standList.size();
			
			for(int i = index; i < index + walkList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(list.get(i)[j]));
					if (j < 41)
						bw.write(",");
				}
				
				bw.newLine();
			}
					
			bw.newLine();
			index += walkList.size();
			
			for(int i = index; i < index + descendStairList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(list.get(i)[j]));
					if (j < 41)
						bw.write(",");
				}
				
				bw.newLine();
			}
						
			bw.newLine();
			index += descendStairList.size();
			
			for(int i = index; i < index + ascendStairList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(list.get(i)[j]));
					if (j < 41)
						bw.write(",");
				}
				
				bw.newLine();
			}
						
			bw.newLine();
			index += ascendStairList.size();
			
			for(int i = index; i < index + runList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(list.get(i)[j]));
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
