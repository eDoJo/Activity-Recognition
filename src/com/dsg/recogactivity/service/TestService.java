package com.dsg.recogactivity.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.dsg.recogactivity.activity.PositionActivity;
import com.dsg.recogactivity.activity.SettingActivity;
import com.dsg.recogactivity.activity.TestActivity;
import com.dsg.recogactivity.constant.MessageDef;
import com.dsg.recogactivity.logic.ActKNN;
import com.dsg.recogactivity.logic.Characterization;
import com.dsg.recogactivity.logic.Characterization2;
import com.dsg.recogactivity.logic.GenerateData;
import com.dsg.recogactivity.logic.KNN;
import com.dsg.recogactivity.logic.PosKNN;
import com.dsg.recogactivity.object.Action;
import com.dsg.recogactivity.object.Feature;
import com.dsg.recogactivity.object.Node;
import com.dsg.recogactivity.object.Position;
import com.dsg.recogactivity.utils.PromptVoice;
import com.dsg.recogactivity.utils.RecordPosAct;
import com.dsg.recogactivity.utils.ToolKits;

public class TestService extends Service implements SensorEventListener {
	private ActKNN mActKNN;
	private PosKNN mPosKNN;
	private PromptVoice mPromptVoice;
	private KNN mKNN;

	private SensorManager mSensorManager;

	public static Timer mTimer = null;
	private TimerTask mTimerTask = null;

	// acc
	public static List<Action> sitList = new LinkedList<Action>();
	public static List<Action> standList = new LinkedList<Action>();
	public static List<Action> walkList = new LinkedList<Action>();
	public static List<Action> descendStairList = new LinkedList<Action>();
	public static List<Action> ascendStairList = new LinkedList<Action>();
	public static List<Action> runList = new LinkedList<Action>();

	// nor acc
	public static List<float[]> sitNorList = new LinkedList<float[]>();
	public static List<float[]> standNorList = new LinkedList<float[]>();
	public static List<float[]> walkNorList = new LinkedList<float[]>();
	public static List<float[]> descendStairNorList = new LinkedList<float[]>();
	public static List<float[]> ascendStairNorList = new LinkedList<float[]>();
	public static List<float[]> runNorList = new LinkedList<float[]>();

	// gyr
	public static List<Action> sitGyrList = new LinkedList<Action>();
	public static List<Action> standGyrList = new LinkedList<Action>();
	public static List<Action> walkGyrList = new LinkedList<Action>();
	public static List<Action> descendStairGyrList = new LinkedList<Action>();
	public static List<Action> ascendStairGyrList = new LinkedList<Action>();
	public static List<Action> runGyrList = new LinkedList<Action>();

	public static List<Position> frontPocket = new LinkedList<Position>();
	public static List<Position> backPocket = new LinkedList<Position>();
	public static List<Position> coatPocket = new LinkedList<Position>();
	public static List<Position> shoulderBag = new LinkedList<Position>();
	public static List<Position> backBag = new LinkedList<Position>();

	// public static final int DELAY_TIME = 500;
	// public static final int Duration = 25;

	public static String[] pos;

	private float[] actX = new float[Characterization.size];
	private float[] actY = new float[Characterization.size];
	private float[] actZ = new float[Characterization.size];
	private float[] posX = new float[Characterization.size];
	private float[] posY = new float[Characterization.size];
	private float[] posZ = new float[Characterization.size];
	private float[] _actX = new float[Characterization.size];
	private float[] _actY = new float[Characterization.size];
	private float[] _actZ = new float[Characterization.size];

	private List<float[]> posTmpList = new LinkedList<float[]>();

	private String path = Environment.getExternalStorageDirectory().getPath()
			+ "/TrainData/";
	private String curAct = "", curPos = "";

	boolean isKnowPosition = false;
	boolean isFirst = true;

	private int option;
	private int preCh = 0;
	private int posCount = 0, actCount1 = 0, actCount2 = 0;

	private float actTempX, actTempY, actTempZ;
	private float posTempX, posTempY, posTempZ;
	private float threshold1 = 5;

	private int isUseSimpleFeature;
	public static int isUseFeedback;
	private int isUseSVM;
	private int isUseSelectedPosition;
	private int count = 0;

	// befor determining the position
	private int thresholdCount = 2;

	private List<Node> list1 = new LinkedList<Node>();
	private List<Node> list2 = new LinkedList<Node>();

	private boolean isLoaded = false;

	private int c1 = 0;
	private int c2 = 0;

	private List<String> actionList1 = new LinkedList<String>();
	private List<String> actionList2 = new LinkedList<String>();

	public static String selAct = "";

	private List<String> recordActList1 = new LinkedList<String>();
	private List<String> recordActList2 = new LinkedList<String>();
	private List<String> recordActList3 = new LinkedList<String>();

	private int calCount1;
	private int calCount2;
	private int calCount3;

	private int windSize = Characterization.size / 2;

	private List<float[]> accDataList = new LinkedList<float[]>();
	private List<float[]> accSVMDataList = new LinkedList<float[]>();

	private List<Float> accX = new LinkedList<Float>();
	private List<Float> accY = new LinkedList<Float>();
	private List<Float> accZ = new LinkedList<Float>();

	private int fileNum = 0;

	private int fbCount = 0;

	/* offine */

	/* offine */

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// mPromptVoice = new PromptVoice(this);
		// mActKNN = new ActKNN(this);
		// mPosKNN = new PosKNN();
		// mKNN = new KNN();
		// mActGYRKNN = new ActGYRKNN(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		// mSensorManager.registerListener(this,
		// mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
		// SensorManager.SENSOR_DELAY_GAME);

		isFirst = true;

		mPromptVoice = new PromptVoice(this);
		mActKNN = new ActKNN(this);
		mPosKNN = new PosKNN();
		mKNN = new KNN();

		initVarible();

		option = (Integer) intent.getExtras().get("option");

		loadSelPosition();
		loadSetting();

		stopTimer();
		// startTimer();
		offProcResult();

		// return super.onStartCommand(intent, flags, startId);
		return START_REDELIVER_INTENT;
	}

	public void clearAccList() {
		sitList.clear();
		standList.clear();
		walkList.clear();
		descendStairList.clear();
		ascendStairList.clear();
		runList.clear();
	}

	public void clearGyrList() {
		sitGyrList.clear();
		standGyrList.clear();
		walkGyrList.clear();
		descendStairGyrList.clear();
		ascendStairGyrList.clear();
		runGyrList.clear();
	}

	public void clearNorList() {
		sitNorList.clear();
		standNorList.clear();
		walkNorList.clear();
		descendStairNorList.clear();
		ascendStairNorList.clear();
		runNorList.clear();
	}

	public void loadSetting() {
		isUseSimpleFeature = ToolKits.getInteger(this,
				SettingActivity.IS_USE_SIMPLE_FEATURE, 0);
		isUseFeedback = ToolKits.getInteger(this,
				SettingActivity.IS_USE_FEEDBACK, 0);
		isUseSVM = ToolKits.getInteger(this, SettingActivity.IS_USE_SVM, 0);
		isUseSelectedPosition = ToolKits.getInteger(this,
				SettingActivity.IS_USE_SELECTED_POSITION, 0);
	}

	public void loadACCData(String path) {
		float[][] sit = new float[TrainService.LENGTH][42];
		float[][] stand = new float[TrainService.LENGTH][42];
		float[][] walk = new float[TrainService.LENGTH][42];
		float[][] descend = new float[TrainService.LENGTH][42];
		float[][] ascend = new float[TrainService.LENGTH][42];
		float[][] run = new float[TrainService.LENGTH][42];

		clearAccList();

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
						sitList.add(new Action(new Feature(sit[i]), "Sit"));
						i++;
						break;
					case 1:
						for (int j = 0; j < 42; j++)
							stand[i][j] = Float.valueOf(temp[j]);
						standList
								.add(new Action(new Feature(stand[i]), "Stand"));
						i++;
						break;
					case 2:
						for (int j = 0; j < 42; j++)
							walk[i][j] = Float.valueOf(temp[j]);
						walkList.add(new Action(new Feature(walk[i]), "Walk"));
						i++;
						break;
					case 3:
						for (int j = 0; j < 42; j++)
							descend[i][j] = Float.valueOf(temp[j]);
						descendStairList.add(new Action(
								new Feature(descend[i]), "DescendStair"));
						i++;
						break;
					case 4:
						for (int j = 0; j < 42; j++)
							ascend[i][j] = Float.valueOf(temp[j]);
						ascendStairList.add(new Action(new Feature(ascend[i]),
								"AscendStair"));
						i++;
						break;
					case 5:
						for (int j = 0; j < 42; j++)
							run[i][j] = Float.valueOf(temp[j]);
						runList.add(new Action(new Feature(run[i]), "Run"));
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

	public void loadGyrData(String path) {
		float[][] sit = new float[TrainService.LENGTH][42];
		float[][] stand = new float[TrainService.LENGTH][42];
		float[][] walk = new float[TrainService.LENGTH][42];
		float[][] descend = new float[TrainService.LENGTH][42];
		float[][] ascend = new float[TrainService.LENGTH][42];
		float[][] run = new float[TrainService.LENGTH][42];

		clearGyrList();

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
						sitGyrList.add(new Action(new Feature(sit[i]), "Sit"));
						i++;
						break;
					case 1:
						for (int j = 0; j < 42; j++)
							stand[i][j] = Float.valueOf(temp[j]);
						standGyrList.add(new Action(new Feature(stand[i]),
								"Stand"));
						i++;
						break;
					case 2:
						for (int j = 0; j < 42; j++)
							walk[i][j] = Float.valueOf(temp[j]);
						walkGyrList
								.add(new Action(new Feature(walk[i]), "Walk"));
						i++;
						break;
					case 3:
						for (int j = 0; j < 42; j++)
							descend[i][j] = Float.valueOf(temp[j]);
						descendStairGyrList.add(new Action(new Feature(
								descend[i]), "DescendStair"));
						i++;
						break;
					case 4:
						for (int j = 0; j < 42; j++)
							ascend[i][j] = Float.valueOf(temp[j]);
						ascendStairGyrList.add(new Action(
								new Feature(ascend[i]), "AscendStair"));
						i++;
						break;
					case 5:
						for (int j = 0; j < 42; j++)
							run[i][j] = Float.valueOf(temp[j]);
						runGyrList.add(new Action(new Feature(run[i]), "Run"));
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

	public void loadSelPosition() {
		String str = ToolKits.getString(this,
				PositionActivity.USER_SELECT_POSITION, null);
		pos = str.split(",");
	}

	public void startTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
		}

		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				int i = 0, time = 0, playCount = 0, waitCount = 11,
						actCount = 22, count = 0, actTestCount = 0;

				boolean isPlayCount = true; // 將手機擺放至固定位置的倒數聲
				boolean isStart = false; // 將手機固定倒數聲至0時，則開始進行下一步驟
				boolean isCollectData = false; // 若為自訂義選項則直接開始收集資料，否則為當指定動作提示聲結束後才開始
				boolean isPlayAct = false; // 是否使用者選中指定動作
				boolean isSpecAct = false; // 撥放指定動作聲
				boolean isSpecFlag = false; // 是否使用者選中指定動作，若是則播放持間倒數聲
				boolean isFirstPlace = true;

				float[] tmp1, tmp2, tmp3, tmp4;

				@Override
				public void run() {
					time += TrainService.Duration;

					// if (isFirstPlace) {
					// mPromptVoice.playPlacement();
					// sleep(2000);
					// isFirstPlace = false;
					// }

					if (isPlayCount) {
						// play countdown voice before run activity
						// if (time % 1000 == 0) {
						// mPromptVoice.playCountdown(playCount--);
						// }

						// play beginning voice
						if (playCount == 0) {
							isPlayCount = false;
							isStart = true;

							sleep(1000);

							if (option == 5) {
								isCollectData = true;
								mPromptVoice.playBegin();
								time = TrainService.Duration;
							} else {
								isSpecAct = true;
								isSpecFlag = true;
								isPlayAct = true;
								time = 0;
							}
						}
					}

					if (isStart) {
						if (isCollectData) {
							// activity
							actX[i] = actTempX;
							actY[i] = actTempY;
							actZ[i] = actTempZ;

							// position
							// posX[i] = posTempX;
							// posY[i] = posTempY;
							// posZ[i] = posTempZ;

							_actX[i] = actTempX;
							_actY[i] = actTempY;
							_actZ[i] = actTempZ;

							i++;

							// Log.i("Dugger", String.valueOf(i));
							if (isSpecFlag) {
								if (time % 1000 == 0 && actCount != 0) {
									mPromptVoice.playCountdown(actCount);
									actCount--;
									Log.d("Dugger", String.valueOf(i));
								}
							}

						} else if (isSpecAct) {
							if (isPlayAct) {
								switch (option) {
								case 1: // walk
									sleep(1000);

									mPromptVoice
											.playWalkTestCountdown(++actTestCount);

									// mPromptVoice.playChangeAct(3);

									selAct = "Walk";
									// Log.d("Dugger", String.valueOf(time));

									sleep(2000);
									break;
								case 2: // descend
									sleep(1000);

									mPromptVoice
											.playDescendTestCountdown(++actTestCount);

									// mPromptVoice.playChangeAct(4);

									selAct = "DescendStair";
									sleep(2000);
									break;
								case 3: // ascend
									sleep(1000);

									mPromptVoice
											.playAscendTestCountdown(++actTestCount);

									// mPromptVoice.playChangeAct(5);

									selAct = "AscendStair";
									sleep(2000);
									break;
								}
							}

							isPlayAct = false;

							if (time % 1000 == 0 && waitCount != 0) {
								mPromptVoice.playCountdown(waitCount--);
								// Log.d("Dugger", String.valueOf(time));
							}

							if (waitCount == 0) {
								sleep(1000);
								mPromptVoice.playBegin();
								isCollectData = true;
								isSpecAct = false;
								time = 0;
								waitCount = 11;

								sleep(2000);
							}
						}

						if (i == Characterization.size) {
							Log.d("Dugger", String.valueOf(time));

							saveAccData(_actX, _actY, _actZ);

							actX = Characterization.dataFilter(actX);
							actY = Characterization.dataFilter(actY);
							actZ = Characterization.dataFilter(actZ);

							// _actX = Characterization2.dataFilter(_actX);
							// _actY = Characterization2.dataFilter(_actY);
							// _actZ = Characterization2.dataFilter(_actZ);

							tmp1 = Characterization.convertToFeature(actX,
									actY, actZ);
							// tmp2 = Characterization.convertToFeature(posX,
							// posY, posZ);

							if (isUseSelectedPosition == 1) {
								if (!TestActivity.curSelPos.equals("")) {
									curPos = TestActivity.curSelPos;

									RecordPosAct.recordPos = curPos;
									tranMsg(MessageDef.UPDATA_POS);

									isKnowPosition = true;
								}
							}

							if (tmp1[1] <= threshold1 && (!isKnowPosition)) {
								RecordPosAct.recordActList.add("Still");
								tranMsg(MessageDef.UPDATA_ACT);
							} else {
								if (!isKnowPosition) {
									if (isUseSelectedPosition == 1) {
										if (!TestActivity.curSelPos.equals("")) {
											curPos = TestActivity.curSelPos;

											RecordPosAct.recordPos = curPos;
											tranMsg(MessageDef.UPDATA_POS);

											isKnowPosition = true;
										}
									} else {
										// only load once data
										if (!isLoaded) {
											loadInfo(path + "walk/data.csv",
													list1, 1);
											loadInfo(path + "run/data.csv",
													list2, 2);

											isLoaded = true;
										}

										tmp3 = tmp1;
										tmp4 = tmp1;

										// featureNor(list1, tmp3);
										// featureNor(list2, tmp4);

										// mKNN.calDistance(list1, tmp3);
										// mKNN.calDistance(list2, tmp4);
										// int type = mKNN.rank();
										// mKNN.clearList(); // clear used list
										//
										// if (type == 1)
										c1 += 1;
										// if (type == 2)
										// c2 += 1;

										if (c1 >= thresholdCount) {
											deterPos(tmp1);
											c1 = 0;
											c2 = 0;
										}

										if (c2 >= thresholdCount) {
											RecordPosAct.recordActList
													.add("Run");
											tranMsg(MessageDef.UPDATA_ACT);
											c2 = 0;
											c1 = 0;
										}
									}
								} else if (isKnowPosition) {
									// writeData(tmp1);

									changeLoadData(1, curPos, 0);

									saveFeatureData(tmp1,
											procSVMFeature(_actX, _actY, _actZ));

									deterAct(tmp1, 0);
									deterAct(tmp1, 1);
									// deterActSVM(procSVMFeature(_actX, _actY,
									// _actZ));
								}
							}

							// the data forward
							// moving(size:Characterization.size)
							for (int j = windSize; j < Characterization.size; j++) {
								actX[j - windSize] = actX[j];
								actY[j - windSize] = actY[j];
								actZ[j - windSize] = actZ[j];

								// posX[j - windSize] = posX[j];
								// posY[j - windSize] = posY[j];
								// posZ[j - windSize] = posZ[j];

								_actX[j - windSize] = _actX[j];
								_actY[j - windSize] = _actY[j];
								_actZ[j - windSize] = _actZ[j];
							}

							i = windSize;
						}

						if (actCount == 0 && count != -1) {
							// Log.i("Dugger", String.valueOf(time));
							// Log.i("Dugger", String.valueOf(i));

							count--;
							isCollectData = false;
							isSpecAct = true;
							isPlayAct = true;
							actCount = 22;
							time = 0;

							if (isUseFeedback == 0) {
								// 1:42 2:Selected 3:SVM
								writeActResult(selAct, 1, curPos);
								writeActResult(selAct, 2, curPos);
								// writeActResult(selAct, 3);

								writeAccData();
								writeFeatureData();
								writeSVMFeatureData();
							} else {
								fbCount += 1;
								Toast.makeText(TestService.this,
										"使用回饋次數 ：" + String.valueOf(fbCount),
										Toast.LENGTH_SHORT).show();
							}

							i = 0;

							initVarible();
						}

						if (count == -1) {
							stopTimer();

							Message msg = new Message();
							msg.what = 2;
							TestActivity.mHandler.sendMessage(msg);

							initVarible();
						}
					}
				}
			};
		}

		if (mTimer != null && mTimerTask != null) {
			mTimer.schedule(mTimerTask, TrainService.DELAY_TIME,
					TrainService.Duration);
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

	public void initVarible() {
		clearAccList();
		clearNorList();
		clearGyrList();

		preCh = 0;

		actCount1 = 0;
		actCount2 = 0;

		calCount1 = 0;
		calCount2 = 0;
		calCount3 = 0;

		posCount = 0;

		actionList1.clear();
		actionList2.clear();

		recordActList1.clear();
		recordActList2.clear();
		recordActList3.clear();

		accDataList.clear();
		accSVMDataList.clear();

		accX.clear();
		accY.clear();
		accZ.clear();

		fileNum = 0;
	}

	public void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void featureNor(List<Node> list, float[] f) {
		List<float[]> _list = new LinkedList<float[]>();

		for (int i = 0; i < list.size(); i++) {
			_list.add(list.get(i).getFeatures());
		}

		_list.add(f);

		_list = Characterization.normalization(_list);

		for (int i = 0; i < list.size() - 1; i++) {
			list.get(i).setFeatures(_list.get(i));
		}

		f = _list.get(list.size() - 1);
	}

	public void loadInfo(String filePath, List<Node> li, int type) {
		float[][] tmp = new float[TrainService.LENGTH * pos.length][42];
		List<float[]> list = new LinkedList<float[]>();

		FileReader fr = null;
		String[] temp = new String[42];

		int lineCount = 0;
		int i = 0;

		try {
			fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();

			while (line != null) {
				if (line.length() > 0) {
					temp = line.split(",");

					switch (lineCount) {
					case 0:
						for (int j = 0; j < 42; j++)
							tmp[i][j] = Float.valueOf(temp[j]);
						list.add(tmp[i]);
						i++;
						break;
					case 1:
						for (int j = 0; j < 42; j++)
							tmp[i][j] = Float.valueOf(temp[j]);
						list.add(tmp[i]);
						i++;
						break;
					case 2:
						for (int j = 0; j < 42; j++)
							tmp[i][j] = Float.valueOf(temp[j]);
						list.add(tmp[i]);
						i++;
						break;
					case 3:
						for (int j = 0; j < 42; j++)
							tmp[i][j] = Float.valueOf(temp[j]);
						list.add(tmp[i]);
						i++;
						break;
					case 4:
						for (int j = 0; j < 42; j++)
							tmp[i][j] = Float.valueOf(temp[j]);
						list.add(tmp[i]);
						i++;
						break;
					}
				} else {
					lineCount++;
				}

				line = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int k = 0; k < list.size(); k++) {
			li.add(new Node(list.get(k), type));
		}
	}

	public void loadPosInfo(String filePath) {
		float[][] tmp = new float[TrainService.LENGTH * pos.length][42];

		FileReader fr = null;
		String[] temp = new String[42];
		int lineCount = 0;
		int i = 0;

		try {
			fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();

			while (line != null) {
				if (line.length() > 0) {
					temp = line.split(",");

					switch (lineCount) {
					case 0:
						for (int j = 0; j < 42; j++)
							tmp[i][j] = Float.valueOf(temp[j]);
						addPosInfo(pos[lineCount], tmp[i]);
						i++;
						break;
					case 1:
						for (int j = 0; j < 42; j++)
							tmp[i][j] = Float.valueOf(temp[j]);
						addPosInfo(pos[lineCount], tmp[i]);
						i++;
						break;
					case 2:
						for (int j = 0; j < 42; j++)
							tmp[i][j] = Float.valueOf(temp[j]);
						addPosInfo(pos[lineCount], tmp[i]);
						i++;
						break;
					case 3:
						for (int j = 0; j < 42; j++)
							tmp[i][j] = Float.valueOf(temp[j]);
						addPosInfo(pos[lineCount], tmp[i]);
						i++;
						break;
					case 4:
						for (int j = 0; j < 42; j++)
							tmp[i][j] = Float.valueOf(temp[j]);
						addPosInfo(pos[lineCount], tmp[i]);
						i++;
						break;
					}
				} else {
					lineCount++;
				}

				line = br.readLine();
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addPosInfo(String pos, float[] tmp) {
		if (pos.equals("frontPocket")) {
			frontPocket.add(new Position(new Feature(tmp), pos));
			posTmpList.add(tmp);
		} else if (pos.equals("backPocket")) {
			backPocket.add(new Position(new Feature(tmp), pos));
			posTmpList.add(tmp);
		} else if (pos.equals("coatPocket")) {
			coatPocket.add(new Position(new Feature(tmp), pos));
			posTmpList.add(tmp);
		} else if (pos.equals("shoulderBag")) {
			shoulderBag.add(new Position(new Feature(tmp), pos));
			posTmpList.add(tmp);
		} else if (pos.equals("backBag")) {
			backBag.add(new Position(new Feature(tmp), pos));
			posTmpList.add(tmp);
		}
	}

	public List<float[]> addPosList() {
		List<float[]> PosList = new LinkedList<float[]>();

		PosList = posTmpList;

		return PosList;
	}

	public void deterPos(float[] tmp) {
		List<Position> _frontPocket = new LinkedList<Position>();
		List<Position> _backPocket = new LinkedList<Position>();
		List<Position> _coatPocket = new LinkedList<Position>();
		List<Position> _backBag = new LinkedList<Position>();
		List<Position> _shoulderBag = new LinkedList<Position>();
		List<float[]> PosList = new LinkedList<float[]>();

		if (isFirst) {
			loadPosInfo(path + "walk/data.csv");
			isFirst = false;
		}

		PosList = addPosList();
		PosList.add(tmp);
		PosList = Characterization.normalization(PosList);

		// Log.d("logger", String.valueOf(PosList.size()));
		// Log.d("logger", String.valueOf(pos.length));

		float[][] avgFeature = new float[pos.length][42];
		float sum = 0;
		for (int i = 0; i < pos.length; i++) {
			for (int j = 0; j < 42; j++) {
				sum = 0;
				for (int k = 0; k < TrainService.LENGTH; k++) {
					sum += PosList.get(i * TrainService.LENGTH + k)[j];
				}

				avgFeature[i][j] = sum / TrainService.LENGTH;
			}
		}

		for (int i = 0; i < pos.length; i++) {
			for (int j = 0; j < 1; j++) {
				if (pos[i].equals("frontPocket")) {
					_frontPocket.add(new Position(new Feature(avgFeature[i]),
							pos[i]));
				} else if (pos[i].equals("backPocket")) {
					_backPocket.add(new Position(new Feature(avgFeature[i]),
							pos[i]));
				} else if (pos[i].equals("coatPocket")) {
					_coatPocket.add(new Position(new Feature(avgFeature[i]),
							pos[i]));
				} else if (pos[i].equals("shoulderBag")) {
					_shoulderBag.add(new Position(new Feature(avgFeature[i]),
							pos[i]));
				} else if (pos[i].equals("backBag")) {
					_backBag.add(new Position(new Feature(avgFeature[i]),
							pos[i]));
					// Log.d("logger", String.valueOf(_backBag.size()));
				}
			}
		}

		Position position = new Position(new Feature(
				PosList.get(PosList.size() - 1)), "");
		mPosKNN.setPosition(position);

		for (int i = 0; i < pos.length; i++) {
			if (pos[i].equals("frontPocket")) {
				mPosKNN.calDistance(_frontPocket, position);
			} else if (pos[i].equals("backPocket")) {
				mPosKNN.calDistance(_backPocket, position);
			} else if (pos[i].equals("coatPocket")) {
				mPosKNN.calDistance(_coatPocket, position);
			} else if (pos[i].equals("shoulderBag")) {
				mPosKNN.calDistance(_shoulderBag, position);
			} else if (pos[i].equals("backBag")) {
				mPosKNN.calDistance(_backBag, position);
			}
		}

		mPosKNN.rank();

		posCount++;

		if (posCount >= 2) {
			curPos = mPosKNN.finVote();

			if (!(curPos.equals("Null"))) {
				RecordPosAct.recordPos = curPos;
				tranMsg(MessageDef.UPDATA_POS);

				isKnowPosition = true;

			}
		}
	}

	public void changeLoadData(int ch, String pos, int isOffineProc) {
		if (preCh != ch) {
			if (isOffineProc == 0) {
				loadACCData(path + pos + "/data_act.csv");
				mActKNN.loadselFeature(pos);
			} else {
				loadACCData(path + "TrainData" + "/" + pos + "/data_act.csv");
				mActKNN.loadselFeature(pos);
			}

			preCh = ch;
		}
	}

	/**
	 * @des 複製動作資料避免更動原本動作資料
	 * @call deterAct呼叫
	 */
	public List<float[]> addActList() {
		List<float[]> actList = new LinkedList<float[]>();

		for (int i = 0; i < sitList.size(); i++) {
			actList.add(sitList.get(i).getFeature().getFeatures());
		}
		for (int i = 0; i < standList.size(); i++) {
			actList.add(standList.get(i).getFeature().getFeatures());
		}

		for (int i = 0; i < walkList.size(); i++) {
			actList.add(walkList.get(i).getFeature().getFeatures());
		}

		for (int i = 0; i < descendStairList.size(); i++) {
			actList.add(descendStairList.get(i).getFeature().getFeatures());
		}

		for (int i = 0; i < ascendStairList.size(); i++) {
			actList.add(ascendStairList.get(i).getFeature().getFeatures());
		}

		for (int i = 0; i < runList.size(); i++) {
			actList.add(runList.get(i).getFeature().getFeatures());
		}

		return actList;
	}

	public void saveNorList(List<float[]> list) {
		int index = 0;

		for (int i = index; i < index + sitList.size(); i++) {
			sitNorList.add(list.get(i));
		}

		index += sitList.size();

		for (int i = index; i < index + standList.size(); i++) {
			standNorList.add(list.get(i));
		}

		index += standList.size();

		for (int i = index; i < index + walkList.size(); i++) {
			walkNorList.add(list.get(i));
		}

		index += walkList.size();

		for (int i = index; i < index + descendStairList.size(); i++) {
			descendStairNorList.add(list.get(i));
		}

		index += descendStairList.size();

		for (int i = index; i < index + ascendStairList.size(); i++) {
			ascendStairNorList.add(list.get(i));
		}

		index += ascendStairList.size();

		for (int i = index; i < index + runList.size(); i++) {
			runNorList.add(list.get(i));
		}
	}

	/**
	 * 
	 * @des 0: 42 features 1:simplified features
	 * @call 需要判別動作時呼叫
	 */
	public void deterAct(float[] tmp, int ch) {
		List<Action> _sitList = new LinkedList<Action>();
		List<Action> _standList = new LinkedList<Action>();
		List<Action> _walkList = new LinkedList<Action>();
		List<Action> _descendStairList = new LinkedList<Action>();
		List<Action> _ascendStairList = new LinkedList<Action>();
		List<Action> _runList = new LinkedList<Action>();

		List<float[]> actList = new LinkedList<float[]>();

		String act = "";

		actList = addActList();
		// actList.add(tmp);
		// Log.d("logger", String.valueOf(actList.size()));
		actList = Characterization.normalization(actList);

		saveNorList(actList);

		// sit;stand;walk;descend;ascend;run
		float[][] avgFeature = new float[6][42];

		float sum = 0;
		int index = 0;
		for (int i = 0; i < 42; i++) {
			sum = 0;
			for (int j = index; j < index + sitList.size(); j++) {
				sum += actList.get(j)[i];
			}
			avgFeature[0][i] = sum / sitList.size();
		}

		index += sitList.size();

		for (int i = 0; i < 42; i++) {
			sum = 0;
			for (int j = index; j < index + standList.size(); j++) {
				sum += actList.get(j)[i];
			}
			avgFeature[1][i] = sum / standList.size();
		}

		index += standList.size();

		for (int i = 0; i < 42; i++) {
			sum = 0;
			for (int j = index; j < index + walkList.size(); j++) {
				sum += actList.get(j)[i];
			}
			avgFeature[2][i] = sum / walkList.size();
		}

		index += walkList.size();

		for (int i = 0; i < 42; i++) {
			sum = 0;
			for (int j = index; j < index + descendStairList.size(); j++) {
				sum += actList.get(j)[i];
			}
			avgFeature[3][i] = sum / descendStairList.size();
		}

		index += descendStairList.size();

		for (int i = 0; i < 42; i++) {
			sum = 0;
			for (int j = index; j < index + ascendStairList.size(); j++) {
				sum += actList.get(j)[i];
			}
			avgFeature[4][i] = sum / ascendStairList.size();
		}

		index += ascendStairList.size();

		for (int i = 0; i < 42; i++) {
			sum = 0;
			for (int j = index; j < index + runList.size(); j++) {
				sum += actList.get(j)[i];
			}
			avgFeature[5][i] = sum / runList.size();
		}

		// float sum = 0;
		// for (int i = 0; i < 6; i++) {
		// for (int j = 0; j < 42; j++) {
		// sum = 0;
		// for (int k = 0; k < TrainService.LENGTH; k++) {
		// sum += actList.get(i * TrainService.LENGTH + k)[j];
		// }
		//
		// avgFeature[i][j] = sum / TrainService.LENGTH;
		// }
		// }

		_sitList.add(new Action(new Feature(avgFeature[0]), "Sit"));
		_standList.add(new Action(new Feature(avgFeature[1]), "Stand"));
		_walkList.add(new Action(new Feature(avgFeature[2]), "Walk"));
		_descendStairList.add(new Action(new Feature(avgFeature[3]),
				"DescendStair"));
		_ascendStairList.add(new Action(new Feature(avgFeature[4]),
				"AscendStair"));
		_runList.add(new Action(new Feature(avgFeature[5]), "Run"));

		// 欲加入的動作節點
		mActKNN.setActNode(new Action(new Feature(tmp), "Null"));

		// mActKNN.setAction(new Action(new Feature(actList.get(actList.size() -
		// 1)), "Null"));
		mActKNN.setAction(new Action(new Feature(Characterization
				.normalization(tmp)), "Null"));

		mActKNN.calDistance(_sitList, ch);
		mActKNN.calDistance(_standList, ch);
		mActKNN.calDistance(_walkList, ch);
		mActKNN.calDistance(_descendStairList, ch);
		mActKNN.calDistance(_ascendStairList, ch);
		mActKNN.calDistance(_runList, ch);

		act = mActKNN.rank();

		if (isUseFeedback == 0) {
			if (ch == 0) {
				actCount1++;

				actionList1.add(act);

				if (actCount1 >= 2 && actionList1.size() >= 2) {

					if (actionList1.get(0).equals(actionList1.get(1))) {
						if (!(actionList1.get(0).equals("Null")))
							curAct = actionList1.get(0);
					}

					actionList1.remove(actionList1.size() - 2);

					if (!(curAct.equals("Null"))) {
						recordActList1.add(curAct);
					}

					if (isUseSVM == 0) {
						if (isUseSimpleFeature == 0) {
							if (!(curAct.equals("Null"))) {
								RecordPosAct.recordActList.add(curAct);
								tranMsg(MessageDef.UPDATA_ACT);
							}
						}
					}
				}
			} else if (ch == 1) {
				actCount2++;

				actionList2.add(act);

				if (actCount2 >= 2 && actionList2.size() >= 2) {
					if (actionList2.get(0).equals(actionList2.get(1))) {
						if (!(actionList2.get(0).equals("Null")))
							curAct = actionList2.get(0);
					}

					actionList2.remove(actionList2.size() - 2);

					if (!(curAct.equals("Null"))) {
						recordActList2.add(curAct);
					}

					if (isUseSVM == 0) {
						if (isUseSimpleFeature == 1) {
							if (!(curAct.equals("Null"))) {
								RecordPosAct.recordActList.add(curAct);
								tranMsg(MessageDef.UPDATA_ACT);
							}
						}
					}
				}
			}
		}
	}

	public void tranMsg(int ch) {
		Message msg = new Message();

		msg.what = ch;
		TestActivity.mHandler.sendMessage(msg);
	}

	public float[] procSVMFeature(float[] _actX, float[] _actY, float[] _actZ) {
		return Characterization2.convertToFeature(_actX, _actY, _actZ, 1);
	}

	public void deterActSVM(float[] tmp) {
		String[] act = { "Sit", "Stand", "Walk", "DescendStair", "AscendStair",
				"Run" };

		int index = evaluate(path + "SVM/" + curPos + "/svmData.model", tmp);

		// RecordPosAct.recordActList.add(act[index]);
		// tranMsg(MessageDef.UPDATA_ACT);
		recordActList3.add(act[index]);

		if (isUseSVM == 1) {
			RecordPosAct.recordActList.add(act[index]);
			tranMsg(MessageDef.UPDATA_ACT);
		}
	}

	public int evaluate(String model_file_path, float[] features) {

		svm_node[] nodes = new svm_node[Characterization.size];

		for (int i = 0; i < Characterization.size; i++) {
			nodes[i] = new svm_node();
			nodes[i].index = i + 1;
			nodes[i].value = features[i];
		}

		svm_model model = null;
		double v = -1;

		try {
			model = svm.svm_load_model(model_file_path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		v = svm.svm_predict(model, nodes);

		Log.d("logger", String.valueOf(v));
		return (int) v;
	}

	public void saveAccData(float[] x, float[] y, float[] z) {
		for (int i = 0; i < x.length; i++) {
			accX.add(x[i]);
			accY.add(y[i]);
			accZ.add(z[i]);
		}
	}

	public void saveFeatureData(float[] tmp, float[] tmp2) {
		float[] _tmp = new float[tmp.length];
		float[] _tmp2 = new float[tmp2.length];

		for (int i = 0; i < tmp.length; i++) {
			_tmp[i] = tmp[i];
		}

		for (int i = 0; i < Characterization.size; i++) {
			_tmp2[i] = tmp2[i];
		}

		accDataList.add(_tmp);
		accSVMDataList.add(_tmp2);
	}

	public void writeAccData() {
		int len = accX.size() / Characterization.size;

		isExitFolder(path + "feedback");
		isExitFolder(path + "feedback" + "/" + curPos);
		isExitFolder(path + "feedback" + "/" + curPos + "/" + selAct);
		isExitFolder(path + "feedback" + "/" + curPos + "/" + selAct + "/"
				+ "Acc");

		for (int i = 0; i < len; i++) {
			try {
				FileWriter fw = new FileWriter(path + "feedback" + "/" + curPos
						+ "/" + selAct + "/" + "Acc" + "/"
						+ String.valueOf(fileNum++) + ".csv", false);
				BufferedWriter bw = new BufferedWriter(fw);

				for (int j = 0; j < Characterization.size; j++) {
					bw.write(String.valueOf(accX.get(i * Characterization.size
							+ j)
							+ ","));
					bw.write(String.valueOf(accY.get(i * Characterization.size
							+ j)
							+ ","));
					bw.write(String.valueOf(accZ.get(i * Characterization.size
							+ j)));

					bw.newLine();
				}

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void writeFeatureData() {

		isExitFolder(path + "feedback");
		isExitFolder(path + "feedback" + "/" + curPos);
		isExitFolder(path + "feedback" + "/" + curPos + "/" + selAct);

		try {
			FileWriter fw = new FileWriter(path + "feedback" + "/" + curPos
					+ "/" + selAct + "/data.csv", false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < accDataList.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(accDataList.get(i)[j]));

					if (i != 41)
						bw.write(",");
				}

				bw.newLine();
			}

			bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void writeSVMFeatureData() {
		isExitFolder(path + "feedback");
		isExitFolder(path + "feedback" + "/" + curPos);
		isExitFolder(path + "feedback" + "/" + curPos + "/" + selAct);
		isExitFolder(path + "feedback" + "/" + curPos + "/" + selAct + "/"
				+ "SVM");

		try {
			FileWriter fw = new FileWriter(path + "feedback" + "/" + curPos
					+ "/" + selAct + "/" + "SVM" + "/data", false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < accSVMDataList.size(); i++) {
				if (selAct.equals("Walk"))
					bw.write(String.valueOf(2));
				if (selAct.equals("DescendStair"))
					bw.write(String.valueOf(3));
				if (selAct.equals("AscendStair"))
					bw.write(String.valueOf(4));

				bw.write(" ");

				for (int j = 0; j < Characterization.size; j++) {
					bw.write(String.valueOf(j + 1));
					bw.write(":");
					bw.write(String.valueOf(accSVMDataList.get(i)[j]));

					if (j != Characterization.size - 1)
						bw.write(" ");
				}

				bw.newLine();
			}

			bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void writeActResult(String fileName, int ch, String pos) {
		switch (ch) {
		case 1:
			calCount1++;

			Log.d("logger", fileName);

			if (calCount1 == 1) {
				isExitFolder(path + "result");
				isExitFolder(path + "result" + "/" + pos);
				isExitFolder(path + "result" + "/" + pos + "/" + "42");

				File file = new File(path + "result" + "/" + pos + "/" + "42"
						+ "/" + fileName + ".txt");

				if (file.exists()) {
					file.delete();
				}

				try {
					FileWriter fw = new FileWriter(path + "result" + "/" + pos
							+ "/" + "42" + "/" + fileName + ".txt", true);
					BufferedWriter bw = new BufferedWriter(fw);

					for (int i = 0; i < recordActList1.size(); i++) {
						bw.write(recordActList1.get(i));
						bw.write(" ");

						if (recordActList1.get(i).equals(fileName)) {
							count++;
						}
					}

					bw.newLine();

					bw.write("Score : ");

					DecimalFormat df = new DecimalFormat("#.##");
					String s = df
							.format(((float) count / recordActList1.size()) * 100);

					count = 0;

					bw.write(String.valueOf(s + "%"));

					bw.newLine();

					bw.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			break;
		case 2:
			calCount2++;

			if (calCount2 == 1) {
				isExitFolder(path + "result");
				isExitFolder(path + "result" + "/" + pos);
				isExitFolder(path + "result" + "/" + pos + "/" + "selected");

				File file = new File(path + "result" + "/" + pos + "/"
						+ "selected" + "/" + fileName + ".txt");

				if (file.exists()) {
					file.delete();
				}
			}

			try {
				FileWriter fw = new FileWriter(path + "result" + "/" + pos
						+ "/" + "selected" + "/" + fileName + ".txt", true);
				BufferedWriter bw = new BufferedWriter(fw);

				for (int i = 0; i < recordActList2.size(); i++) {
					bw.write(recordActList2.get(i));
					bw.write(" ");

					if (recordActList2.get(i).equals(fileName)) {
						count++;
					}
				}

				bw.newLine();

				bw.write("Score : ");

				DecimalFormat df = new DecimalFormat("#.##");
				String s = df
						.format(((float) count / recordActList2.size()) * 100);

				count = 0;

				bw.write(String.valueOf(s + "%"));

				bw.newLine();

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			break;
		case 3:
			calCount3++;

			if (calCount3 == 1) {
				isExitFolder(path + "result");
				isExitFolder(path + "result" + "/" + pos);
				isExitFolder(path + "result" + "/" + pos + "/" + "SVM");

				File file = new File(path + "result" + "/" + pos + "/" + "SVM"
						+ "/" + fileName + ".txt");

				if (file.exists()) {
					file.delete();
				}
			}

			try {
				FileWriter fw = new FileWriter(path + "result" + "/" + pos
						+ "/" + "SVM" + "/" + fileName + ".txt", true);
				BufferedWriter bw = new BufferedWriter(fw);

				for (int i = 0; i < recordActList3.size(); i++) {
					bw.write(recordActList3.get(i));
					bw.write(" ");

					if (recordActList3.get(i).equals(fileName)) {
						count++;
					}
				}

				bw.newLine();

				bw.write("Score : ");

				DecimalFormat df = new DecimalFormat("#.##");
				String s = df
						.format(((float) count / recordActList3.size()) * 100);

				count = 0;

				bw.write(String.valueOf(s + "%"));

				bw.newLine();

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			break;
		}
	}

	public void isExitFolder(String s) {
		File file = new File(s);

		if (!file.exists()) {
			file.mkdir();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopTimer();
		mSensorManager.unregisterListener(this);
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

	/* offline data recognition */

	public void offProcResult() {
		List<float[]> list;

		// String[] curPos = { "frontPocket", "backPocket", "coatPocket",
		// "shoulderBag", "backBag" };
		// String[] curPos = { "frontPocket", "backPocket", "coatPocket" };
		// String[] curPos = { "shoulderBag", "backBag" };
		String[] curPos = { "backBag" };
		String[] selAct = { "Walk", "DescendStair", "AscendStair" };

		
		
		isUseFeedback = 0;

		for (int i = 0; i < curPos.length; i++) {
			for (int j = 0; j < selAct.length; j++) {
				initVarible();
				changeLoadData(1, curPos[i], 0);

				// list = getTestData(path + "feedback" + "/" + curPos[i] + "/"
				// + selAct[j] + "/" + "data.csv");

				list = new LinkedList<float[]>();
				for (int l = 0; l < 21; l++) {
					loadAccData(path + "feedback" + "/" + curPos[i] + "/"
							+ selAct[j] + "/Acc" + "/" + String.valueOf(l)
							+ ".csv", list);
				}

				for (int k = 0; k < list.size(); k++) {
					deterAct(list.get(k), 0);
				}

				writeActResult(selAct[j], 1, curPos[i]);
			}

			for (int j = 0; j < selAct.length; j++) {
				initVarible();
				changeLoadData(1, curPos[i], 0);

				// list = getTestData(path + "feedback" + "/" + curPos[i] + "/"
				// + selAct[j] + "/" + "data.csv");
				list = new LinkedList<float[]>();
				for (int l = 0; l < 21; l++) {
					loadAccData(path + "feedback" + "/" + curPos[i] + "/"
							+ selAct[j] + "/Acc" + "/" + String.valueOf(l)
							+ ".csv", list);
				}

				for (int k = 0; k < list.size(); k++) {
					deterAct(list.get(k), 1);
				}

				writeActResult(selAct[j], 2, curPos[i]);
			}
		}

		for (int i = 0; i < curPos.length; i++) {
			for (int j = 0; j < selAct.length; j++) {
				initVarible();
				changeLoadData(1, "_" + curPos[i], 1);

				list = getTestData(path + "feedback" + "/" + curPos[i] + "/"
						+ selAct[j] + "/" + "data.csv");

				list = new LinkedList<float[]>();
				for (int l = 0; l < 21; l++) {
					loadAccData(path + "feedback" + "/" + curPos[i] + "/"
							+ selAct[j] + "/Acc" + "/" + String.valueOf(l)
							+ ".csv", list);
				}

				for (int k = 0; k < list.size(); k++) {
					deterAct(list.get(k), 0);
				}

				_writeActResult(selAct[j], 1, curPos[i]);
			}

			for (int j = 0; j < selAct.length; j++) {
				initVarible();
				changeLoadData(1, "_" + curPos[i], 1);

				list = getTestData(path + "feedback" + "/" + curPos[i] + "/"
						+ selAct[j] + "/" + "data.csv");

				list = new LinkedList<float[]>();
				for (int l = 0; l < 21; l++) {
					loadAccData(path + "feedback" + "/" + curPos[i] + "/"
							+ selAct[j] + "/Acc" + "/" + String.valueOf(l)
							+ ".csv", list);
				}

				for (int k = 0; k < list.size(); k++) {
					deterAct(list.get(k), 1);
				}

				_writeActResult(selAct[j], 2, curPos[i]);
			}
		}

		// changeLoadData(1, "_" + curPos, 1);
		// list = getTestData(path + "feedback" + "/" + curPos + "/" + selAct
		// + "/" + "data.csv");
		//
		// for (int i = 5; i < list.size(); i++) {
		// deterAct(list.get(i), 0);
		// }

		// initVarible();

		// writeActResult(selAct, 1);
		// writeActResult(selAct, 2);

		Message msg = new Message();
		msg.what = 2;
		TestActivity.mHandler.sendMessage(msg);
	}

	public List<float[]> getTestData(String path) {
		List<float[]> list = new LinkedList<float[]>();
		float[][] feature = new float[21][42];

		FileReader fr = null;
		String[] temp = new String[42];
		int i = 0;

		try {
			fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();

			while (line != null) {
				if (line.length() > 0) {
					temp = line.split(",");

					for (int j = 0; j < 42; j++)
						feature[i][j] = Float.valueOf(temp[j]);
					list.add(feature[i]);
				}

				line = br.readLine();
				i++;
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

	public void _writeActResult(String fileName, int ch, String pos) {
		switch (ch) {
		case 1:
			calCount1++;

			if (calCount1 == 1) {
				isExitFolder(path + "TrainData" + "/" + "result");
				isExitFolder(path + "TrainData" + "/" + "result" + "/" + pos);
				isExitFolder(path + "TrainData" + "/" + "result" + "/" + pos
						+ "/" + "42");

				File file = new File(path + "TrainData" + "/" + "result" + "/"
						+ pos + "/" + "42" + "/" + fileName + ".txt");

				if (file.exists()) {
					file.delete();
				}

				try {
					FileWriter fw = new FileWriter(path + "TrainData" + "/"
							+ "result" + "/" + pos + "/" + "42" + "/"
							+ fileName + ".txt", true);
					BufferedWriter bw = new BufferedWriter(fw);

					for (int i = 0; i < recordActList1.size(); i++) {
						bw.write(recordActList1.get(i));
						bw.write(" ");

						if (recordActList1.get(i).equals(fileName)) {
							count++;
						}
					}

					bw.newLine();

					bw.write("Score : ");

					DecimalFormat df = new DecimalFormat("#.##");
					String s = df
							.format(((float) count / recordActList1.size()) * 100);

					count = 0;

					bw.write(String.valueOf(s + "%"));

					bw.newLine();

					bw.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			break;
		case 2:
			calCount2++;

			if (calCount2 == 1) {
				isExitFolder(path + "TrainData" + "/" + "result");
				isExitFolder(path + "TrainData" + "/" + "result" + "/" + pos);
				isExitFolder(path + "TrainData" + "/" + "result" + "/" + pos
						+ "/" + "selected");

				File file = new File(path + "TrainData" + "/" + "result" + "/"
						+ pos + "/" + "selected" + "/" + fileName + ".txt");

				if (file.exists()) {
					file.delete();
				}
			}

			try {
				FileWriter fw = new FileWriter(path + "TrainData" + "/"
						+ "result" + "/" + pos + "/" + "selected" + "/"
						+ fileName + ".txt", true);
				BufferedWriter bw = new BufferedWriter(fw);

				for (int i = 0; i < recordActList2.size(); i++) {
					bw.write(recordActList2.get(i));
					bw.write(" ");

					if (recordActList2.get(i).equals(fileName)) {
						count++;
					}
				}

				bw.newLine();

				bw.write("Score : ");

				DecimalFormat df = new DecimalFormat("#.##");
				String s = df
						.format(((float) count / recordActList2.size()) * 100);

				count = 0;

				bw.write(String.valueOf(s + "%"));

				bw.newLine();

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			break;
		case 3:
			calCount3++;

			if (calCount3 == 1) {
				isExitFolder(path + "TrainData" + "/" + "result");
				isExitFolder(path + "TrainData" + "/" + "result" + "/" + pos);
				isExitFolder(path + "TrainData" + "/" + "result" + "/" + pos
						+ "/" + "SVM");

				File file = new File(path + "TrainData" + "/" + "result" + "/"
						+ pos + "/" + "SVM" + "/" + fileName + ".txt");

				if (file.exists()) {
					file.delete();
				}
			}

			try {
				FileWriter fw = new FileWriter(path + "TrainData" + "/"
						+ "result" + "/" + pos + "/" + "SVM" + "/" + fileName
						+ ".txt", true);
				BufferedWriter bw = new BufferedWriter(fw);

				for (int i = 0; i < recordActList3.size(); i++) {
					bw.write(recordActList3.get(i));
					bw.write(" ");

					if (recordActList3.get(i).equals(fileName)) {
						count++;
					}
				}

				bw.newLine();

				bw.write("Score : ");

				DecimalFormat df = new DecimalFormat("#.##");
				String s = df
						.format(((float) count / recordActList3.size()) * 100);

				count = 0;

				bw.write(String.valueOf(s + "%"));

				bw.newLine();

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			break;
		}
	}

	/* offline data recognition */

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
}
