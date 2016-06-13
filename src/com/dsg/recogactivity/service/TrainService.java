package com.dsg.recogactivity.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;

import com.dsg.recogactivity.activity.TrainActivity;
import com.dsg.recogactivity.constant.MessageDef;
import com.dsg.recogactivity.logic.Characterization;
import com.dsg.recogactivity.logic.Characterization2;
import com.dsg.recogactivity.utils.PromptVoice;

public class TrainService extends Service implements SensorEventListener {
	private SensorManager mSensorManager;

	/** control running time and frequency */
	public static Timer mTimer = null;
	private TimerTask mTimerTask = null;

	private PromptVoice mPromptVoice;

	public static int actNum = 1; // play activity sound by order
	public static final int LENGTH = 10; //
	public static final int TOTAL_LENGTH = 20; // inital length
	public static final int DELAY_TIME = 500;
	public static final int Duration = 25;

	public static List<float[]> actList = new LinkedList<float[]>();
	public static List<float[]> posList = new LinkedList<float[]>();

	/** take twenty seconds for each activity */
	private float[] actX = new float[Characterization.size * TOTAL_LENGTH];
	private float[] actY = new float[Characterization.size * TOTAL_LENGTH];
	private float[] actZ = new float[Characterization.size * TOTAL_LENGTH];
	private float[] posX = new float[Characterization.size * TOTAL_LENGTH];
	private float[] posY = new float[Characterization.size * TOTAL_LENGTH];
	private float[] posZ = new float[Characterization.size * TOTAL_LENGTH];

	private float[] _actX = new float[Characterization.size * TOTAL_LENGTH];
	private float[] _actY = new float[Characterization.size * TOTAL_LENGTH];
	private float[] _actZ = new float[Characterization.size * TOTAL_LENGTH];

	private float actTempX, actTempY, actTempZ;
	private float posTempX, posTempY, posTempZ;

	private String path = Environment.getExternalStorageDirectory().getPath()
			+ "/TrainData/";
	private String selPos;

	private int fileNum = 0;

	private String[] act = { "sit", "stand", "walk", "descend", "ascend", "run" };

	public static List<float[]> actSVMList = new LinkedList<float[]>();

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_GAME);

		clearList();

		mPromptVoice = new PromptVoice(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		selPos = (String) intent.getExtras().get("SelPos");

		isExitFolder(path);
		isExitFolder(path + selPos);

		stopTimer();
		startTimer();

		// return super.onStartCommand(intent, flags, startId);
		return START_REDELIVER_INTENT;
	}

	public void startTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
		}

		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				int i = 0;
				int time = 0;
				int waitCount = 11;
				int actCount = 20;

				boolean isPlayChange = true;
				boolean isPlayCount = false;
				boolean isStartRecord = false;

				@Override
				public void run() {
					time += Duration;

					// play voice which switches activity
					if (isPlayChange) {
						mPromptVoice.playChangeAct(actNum);
						isPlayChange = false;
						isPlayCount = true;

						sleep(1500);
					}

					if (isPlayCount) {
						// play countdown voice before run activity
						if (time % 1000 == 0) {
							mPromptVoice.playCountdown(waitCount--);
						}

						// play beginning voice
						if (waitCount == 0) {
							sleep(1000);
							mPromptVoice.playBegin();

							isPlayCount = false;
							isStartRecord = true;

							actCount = 20;
							time = Duration;

							sleep(2000);
						}
					}

					if (isStartRecord) {
						if (time % 1000 == 0 && actCount != 0) {
							mPromptVoice.playCountdown(actCount);
							actCount--;
						}

						// activity
						actX[i] = actTempX;
						actY[i] = actTempY;
						actZ[i] = actTempZ;
						// position
						posX[i] = posTempX;
						posY[i] = posTempY;
						posZ[i] = posTempZ;

						_actX[i] = actTempX;
						_actY[i] = actTempY;
						_actZ[i] = actTempZ;

						// update position and activity data on the textview
						tranMsg(MessageDef.UPDATA_ACT_POS_INFO);

						i++; // increment i by one, when each run once

						if (i >= 80 && i != Characterization.size * TOTAL_LENGTH) {
							if (i % 40 == 0) {
								for (int j = 0; j < 40; j++) {
									actX[i + j] = actX[i - 40 + j];
									actY[i + j] = actY[i - 40 + j];
									actZ[i + j] = actZ[i - 40 + j];

									posX[i + j] = posX[i - 40 + j];
									posY[i + j] = posY[i - 40 + j];
									posZ[i + j] = posZ[i - 40 + j];

									_actX[i + j] = _actX[i - 40 + j];
									_actY[i + j] = _actY[i - 40 + j];
									_actZ[i + j] = _actZ[i - 40 + j];
								}

								i += 40;
							}
						}

						if (i == Characterization.size * TOTAL_LENGTH) {

							convFeatureData();
							convSVMFeatureData();

							writeAccData(act[actNum - 1]); // 寫入所有三軸加速器的所有資料

							fileNum = 0; // 寫完三軸資料後歸0

							i = 0;
							waitCount = 11;

							isStartRecord = false;
							isPlayChange = true;

							actNum++;

							sleep(1000);
						}

						if (actNum == 7 && isPlayChange) {
							actNum = 1;

							writeFeatureData(actList, "/data_act.csv");
							writeFeatureData(posList, "/data_pos.csv");

							actList = Characterization.normalization(actList);
							writeFeatureData(actList, "/data_act_normal.csv");

							posList = Characterization.normalization(posList);

							trainSVMModel(actSVMList);
							writeSVMFormatData(actSVMList);

							tranMsg(MessageDef.UPDATA_BUTTON_TEXT);
							tranMsg(MessageDef.UPDATA_BUTTON_STATUS);

							actList.clear();
							posList.clear();

							stopTimer();
						}
					}
				}
			};
		}

		if (mTimer != null && mTimerTask != null) {
			mTimer.schedule(mTimerTask, DELAY_TIME, Duration);
		}
	}

	public void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

	public static void cancel() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	public void tranMsg(int ch) {
		Message msg = new Message();
		Bundle data = new Bundle();

		switch (ch) {
		case 0:
			String s = actTempX + "," + actTempY + "," + actTempZ + ","
					+ posTempX + "," + posTempY + "," + posTempZ;

			data.putString("data", s);
			msg.setData(data);
			break;
		}

		msg.what = ch;
		TrainActivity.mHandler.sendMessage(msg);
	}

	public void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void convFeatureData() {
		List<float[]> list1 = new LinkedList<float[]>();
		List<float[]> list2 = new LinkedList<float[]>();

		float[][] x1 = new float[TOTAL_LENGTH][Characterization.size];
		float[][] y1 = new float[TOTAL_LENGTH][Characterization.size];
		float[][] z1 = new float[TOTAL_LENGTH][Characterization.size];

		float[][] x2 = new float[TOTAL_LENGTH][Characterization.size];
		float[][] y2 = new float[TOTAL_LENGTH][Characterization.size];
		float[][] z2 = new float[TOTAL_LENGTH][Characterization.size];
		
		float[] _x1 = new float[Characterization.size];
		float[] _y1 = new float[Characterization.size];
		float[] _z1 = new float[Characterization.size];

		float[] _x2 = new float[Characterization.size];
		float[] _y2 = new float[Characterization.size];
		float[] _z2 = new float[Characterization.size];

		list1.clear();
		list2.clear();

		for (int i = 0; i < TOTAL_LENGTH; i++) {
			for (int j = 0; j < Characterization.size; j++) {

				x1[i][j] = actX[i * Characterization.size + j];
				y1[i][j] = actY[i * Characterization.size + j];
				z1[i][j] = actZ[i * Characterization.size + j];

				x2[i][j] = posX[i * Characterization.size + j];
				y2[i][j] = posY[i * Characterization.size + j];
				z2[i][j] = posZ[i * Characterization.size + j];
			}
		}

		for (int i = 0; i < TOTAL_LENGTH; i++) {
			_x1 = Characterization.dataFilter(x1[i]);
			_y1 = Characterization.dataFilter(y1[i]);
			_z1 = Characterization.dataFilter(z1[i]);

			_x2 = Characterization.dataFilter(x2[i]);
			_y2 = Characterization.dataFilter(y2[i]);
			_z2 = Characterization.dataFilter(z2[i]);
			
//			_x1 = x1[i];
//			_y1 = y1[i];
//			_z1 = z1[i];
//					
//			_x2 = x2[i];
//			_y2 = y2[i];
//			_z2 = z2[i];
			

			// The data convert to features
			list1.add(Characterization.convertToFeature(_x1, _y1, _z1));
			list2.add(Characterization.convertToFeature(_x2, _y2, _z2));
		}

		// remove noisy data
		// for (int i = 0; i < TOTAL_LENGTH - LENGTH; i++) {
		// list1.remove(getLongDistPos(list1));
		// list2.remove(getLongDistPos(list2));
		// }

		for (int i = 0; i < LENGTH; i++) {
			actList.add(list1.get(i));
			posList.add(list2.get(i));
		}
	}

	public void convSVMFeatureData() {
		List<float[]> list1 = new LinkedList<float[]>();

		float[][] x1 = new float[LENGTH][Characterization.size];
		float[][] y1 = new float[LENGTH][Characterization.size];
		float[][] z1 = new float[LENGTH][Characterization.size];
		
		float[] _x = new float[Characterization.size];
		float[] _y = new float[Characterization.size];
		float[] _z = new float[Characterization.size];

		for (int i = 0; i < LENGTH; i++) {
			for (int j = 0; j < Characterization.size; j++) {
				x1[i][j] = _actX[i * Characterization.size + j];
				y1[i][j] = _actY[i * Characterization.size + j];
				z1[i][j] = _actZ[i * Characterization.size + j];
			}
			
			_x = Characterization2.dataFilter(x1[i]);
			_y = Characterization2.dataFilter(y1[i]);
			_z = Characterization2.dataFilter(z1[i]);

			list1.add(Characterization2
					.convertToFeature(_x, _y, _z, 1));
		}

		for (int i = 0; i < LENGTH; i++) {
			actSVMList.add(list1.get(i));
		}
	}

	public int getLongDistPos(List<float[]> list) {
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

		for (int i = 0; i < distance.length; i++)
			distance[i] = 0;

		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < 42; j++) {
				distance[i] = (float) (distance[i] + Math.pow(list.get(i)[j]
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
	 * 
	 * @des 寫入每個位置的三軸加速器與陀螺儀的值
	 * @call 收集足夠的資料
	 */
	public void writeFeatureData(List<float[]> list, String fileName) {
		try {
			FileWriter fw = new FileWriter(path + selPos + fileName, false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < list.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(list.get(i)[j]));
					if (j < 41)
						bw.write(",");
				}

				bw.newLine();

				if (i % LENGTH == LENGTH - 1 && i != list.size() - 1) {
					bw.newLine();
				}
			}

			bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void writeAccData(String act) {
		isExitFolder(path + selPos + "/" + act);

		for (int i = 0; i < LENGTH; i++) {
			try {
				FileWriter fw = new FileWriter(path + selPos + "/" + act + "/"
						+ String.valueOf(fileNum++) + ".csv", false);
				BufferedWriter bw = new BufferedWriter(fw);

				for (int j = 0; j < Characterization.size; j++) {
					bw.write(String
							.valueOf(_actX[i * Characterization.size + j])
							+ ",");
					bw.write(String
							.valueOf(_actY[i * Characterization.size + j])
							+ ",");
					bw.write(String
							.valueOf(_actZ[i * Characterization.size + j]));

					bw.newLine();
				}

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	// 20 - 59
	public void writeSVMFormatData(List<float[]> list) {

		try {
			FileWriter fw = new FileWriter(path + "SVM" + "/" + selPos
					+ "/svmData", false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < list.size() - 2 * LENGTH; i++) {
				bw.write(String.valueOf((i + 2 * LENGTH) / LENGTH));
				bw.write(" ");

				for (int j = 0; j < Characterization.size; j++) {
					bw.write(String.valueOf(j + 1));
					bw.write(":");
					bw.write(String.valueOf(list.get(i + 2 * LENGTH)[j]));

					if (j < Characterization.size - 1)
						bw.write(" ");
				}

				bw.newLine();
			}

			bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// 20 - 59
	public void trainSVMModel(List<float[]> list) {
		String model_file_path = path + "SVM" + "/" + selPos + "/svmData.model";

		isExitFolder(path + "SVM");
		isExitFolder(path + "SVM" + "/" + selPos);

		svm_parameter param = new svm_parameter();

		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0.025;
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];

		svm_problem prob = new svm_problem();
		int dataCount = 4 * LENGTH;

		prob.y = new double[dataCount];
		prob.l = dataCount;
		prob.x = new svm_node[dataCount][];

		for (int i = 0; i < dataCount; i++) {
			prob.x[i] = new svm_node[Characterization.size];

			for (int j = 0; j < Characterization.size; j++) {
				svm_node node = new svm_node();
				node.index = j + 1;
				node.value = list.get(i + 2 * LENGTH)[j];
				prob.x[i][j] = node;
			}

			prob.y[i] = (i + 2 * LENGTH) / LENGTH;
			// prob.y[i] = i;
		}

		try {
			svm_model model = svm.svm_train(prob, param);
			svm.svm_save_model(model_file_path, model);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void isExitFolder(String s) {
		File file = new File(s);

		if (!file.exists()) {
			file.mkdir();
		}
	}

	public static void clearList() {
		actList.clear();
		posList.clear();
		actSVMList.clear();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			actTempX = event.values[0];
			actTempY = event.values[1];
			actTempZ = event.values[2];
			break;
		case Sensor.TYPE_GYROSCOPE:
			posTempX = event.values[0];
			posTempY = event.values[1];
			posTempZ = event.values[2];
			break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mSensorManager.unregisterListener(this);
		stopTimer();
	}
}
