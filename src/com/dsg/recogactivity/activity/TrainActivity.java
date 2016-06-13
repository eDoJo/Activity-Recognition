package com.dsg.recogactivity.activity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dsg.recogactivity.R;
import com.dsg.recogactivity.constant.MessageDef;
import com.dsg.recogactivity.logic.Characterization;
import com.dsg.recogactivity.logic.GenerateData;
import com.dsg.recogactivity.logic.NodeProc;
import com.dsg.recogactivity.logic.SelFeature;
import com.dsg.recogactivity.service.TrainService;
import com.dsg.recogactivity.utils.ToolKits;

public class TrainActivity extends Activity implements OnClickListener,
		OnItemSelectedListener {
	private Spinner spnPosition;
	private TextView tv1, tv2, tv3;
	private Button btnRun, btnStop, btnComplete;

	public static Handler mHandler;
	
	public static final String IS_TRAIN_FINISH = "is_train_finish";	
	
	public static String[] pos;
	
	private List<float[]> walkPosList = new LinkedList<float[]>();
	private List<float[]> runPosList = new LinkedList<float[]>();
	private List<float[]> ascendPosList = new LinkedList<float[]>();
	private List<float[]> descendPosList = new LinkedList<float[]>();

	private List<float[]> walkNorPosList = new LinkedList<float[]>();
	
	private String[] position;

	private String path = Environment.getExternalStorageDirectory().getPath()
			+ "/TrainData/";
	private String selFeature;
	/** user select position currently */
	private String curSelPos = "";
	private String selPos = "";

	private int flag = 1;

	private long touchTime = 0;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/** remain screen turned on */
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (ToolKits.getBoolean(this, IS_TRAIN_FINISH, false)) {
			Intent intent = new Intent(this, TestActivity.class);
			startActivity(intent);

			finish();
		} else {
			setContentView(R.layout.activity_train);

			initView();
			initHandler();
		}
	}

	public void initView() {
		spnPosition = (Spinner) findViewById(R.id.spn_position);

		getSelPos();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, position);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		spnPosition.setAdapter(adapter);
		spnPosition.setOnItemSelectedListener(this);

		btnRun = (Button) findViewById(R.id.btn_run);
		btnStop = (Button) findViewById(R.id.btn_stop);
		btnComplete = (Button) findViewById(R.id.btn_complete);

		btnRun.setOnClickListener(this);
		btnStop.setOnClickListener(this);
		btnComplete.setOnClickListener(this);

		tv1 = (TextView) findViewById(R.id.textView1);
		tv2 = (TextView) findViewById(R.id.textView2);
		tv3 = (TextView) findViewById(R.id.textView3);
	}

	/**
	 * get previous activity selected position
	 */
	public void getSelPos() {
		String str = ToolKits.getString(this,
				PositionActivity.USER_SELECT_POSITION, null);
		position = str.split(",");
	}

	public void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MessageDef.UPDATA_ACT_POS_INFO:
					String[] tmp = msg.getData().getString("data").split(",");

					tv1.setText("ActX:" + tmp[0] + "\n" + "PosX:" + tmp[3]);
					tv2.setText("ActX:" + tmp[1] + "\n" + "PosX:" + tmp[4]);
					tv3.setText("ActX:" + tmp[2] + "\n" + "PosX:" + tmp[5]);
					break;
				case MessageDef.UPDATA_BUTTON_TEXT: // 當一系列動作訓練完成時，改成按鈕的文字
					btnRun.setText("Run");
					flag = 1;
					break;
				}

				super.handleMessage(msg);
			}
		};
	}

	@Override
	public void onClick(View v) {
		Intent mIntent = new Intent(this, TrainService.class);

		switch (v.getId()) {
		case R.id.btn_run:
			// start service with bundle
			if (flag == 1) {
				mIntent.putExtra("SelPos", curSelPos);
				startService(mIntent);

				btnRun.setText("pause");

				flag = 2;
			} else {
				TrainService.cancel();

				btnRun.setText("Run");

				flag = 1;
			}

			break;
		case R.id.btn_stop:
			new AlertDialog.Builder(TrainActivity.this)
					.setTitle("提示")
					.setMessage("確定要停止並刪除之前動作紀錄 嗎?")
					.setNegativeButton("否",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							})
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									TrainService.cancel();

									btnRun.setText("Run");

									TrainService.actNum = 1; // 停止時，初始回坐著動作

									// 清除List資料，避免位置與動作資料重複
//									TrainService.actList.clear();
//									TrainService.posList.clear();
									
									TrainService.clearList();
									
									flag = 1;
								}
							}).show();

			break;
		case R.id.btn_complete:
			boolean flag = true;

			for (int i = 0; i < position.length; i++) {
				// 判斷是否使用者所挑選的位置與其對應的訓練動作是否完整
				if (!(isExitFile(path + position[i] + "/data_act_normal.csv"))) {
					flag = false;
					break;
				}
			}

			if (flag) {
				if (isServiceRunning(this,
						"com.dsg.recogactivity.service.TrainService")) {
					stopService(mIntent);
				}

				ToolKits.putBoolean(this, IS_TRAIN_FINISH, true);

				SelFeature mSelFesture = new SelFeature();
				for (int i = 0; i < position.length; i++) {
					// 依據位置挑選所需特徵
					selFeature = mSelFesture.getSelectFesture(path
							+ position[i] + "/data_act_normal.csv");

					// 紀錄每個位置的挑選特徵
					ToolKits.putString(this, position[i], selFeature);
					
					Log.d("logger", selFeature);
				}

				for (int i = 0; i < position.length; i++) {
					selPos = position[i];
					// load same activity data from different position
					getActData(path + position[i] + "/data_act.csv");
				}

				walkNorPosList = walkPosList;
				walkNorPosList = Characterization.normalization(walkNorPosList);

				writeWekaData(walkNorPosList, 5);
				writeWekaData(walkNorPosList, 6);

				writeSelActData(walkPosList, "walk");
				writeSelActData(runPosList, "run");

				Toast.makeText(TrainActivity.this, "完成訓練!!!",
						Toast.LENGTH_SHORT).show();
				
				
				/**/
				
				GenerateData mGenerateData = new GenerateData();
				mGenerateData.generData(position);
				
				// path + "TrainData" + "/" + "_" + curPos[i])
				SelFeature _mSelFesture = new SelFeature();
				for (int i = 0; i < position.length; i++) {
					// 依據位置挑選所需特徵
					selFeature = _mSelFesture.getSelectFesture(path + "TrainData" + "/"
							+ "_" + position[i] + "/data_act_normal.csv");

					// 紀錄每個位置的挑選特徵
					ToolKits.putString(this, "_" +  position[i], selFeature);
				}
				
				/**/
				

				Intent intent = new Intent(this, TestActivity.class);
				startActivity(intent);

				finish();
			} else {
				Toast.makeText(TrainActivity.this, "您尚未訓練完畢!!!",
						Toast.LENGTH_SHORT).show();
				flag = true;
			}

			break;
		}
	}

	public void getActData(String path) {
		List<float[]> tmp = new LinkedList<float[]>();

		// tmp <- list(record all activitys:sit;stand;walk;run;ascend;descend)
		tmp = loadAllActData(path);

		writeWekaData(tmp, 1);
		writeWekaData(tmp, 2);
		writeWekaData(tmp, 3);
		writeWekaData(tmp, 4);

		// 填充相對應的走路、跑步、上下樓梯的List資料(sit;stand;walk,descend,ascend,run)
		for (int i = 2 * TrainService.LENGTH; i < 3 * TrainService.LENGTH; i++)
			walkPosList.add(tmp.get(i));

		for (int i = 3 * TrainService.LENGTH; i < 4 * TrainService.LENGTH; i++)
			descendPosList.add(tmp.get(i));

		for (int i = 4 * TrainService.LENGTH; i < 5 * TrainService.LENGTH; i++)
			ascendPosList.add(tmp.get(i));

		for (int i = 5 * TrainService.LENGTH; i < 6 * TrainService.LENGTH; i++)
			runPosList.add(tmp.get(i));
	}

	public List<float[]> loadAllActData(String path) {
		List<float[]> list = new LinkedList<float[]>();

		float[][] sit = new float[TrainService.LENGTH][42];
		float[][] stand = new float[TrainService.LENGTH][42];
		float[][] walk = new float[TrainService.LENGTH][42];
		float[][] run = new float[TrainService.LENGTH][42];
		float[][] ascend = new float[TrainService.LENGTH][42];
		float[][] descend = new float[TrainService.LENGTH][42];

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
						list.add(sit[i]);
						i++;
						break;
					case 1:
						for (int j = 0; j < 42; j++)
							stand[i][j] = Float.valueOf(temp[j]);
						list.add(stand[i]);
						i++;
						break;
					case 2:
						for (int j = 0; j < 42; j++)
							walk[i][j] = Float.valueOf(temp[j]);
						list.add(walk[i]);
						i++;
						break;
					case 3:
						for (int j = 0; j < 42; j++)
							descend[i][j] = Float.valueOf(temp[j]);
						list.add(descend[i]);
						i++;
						break;
					case 4:
						for (int j = 0; j < 42; j++)
							ascend[i][j] = Float.valueOf(temp[j]);
						list.add(ascend[i]);
						i++;
						break;
					case 5:
						for (int j = 0; j < 42; j++)
							run[i][j] = Float.valueOf(temp[j]);
						list.add(run[i]);
						i++;
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

		return list;
	}

	/**
	 * 
	 * @des 分別寫入走路與跑步的不同位置資料(如走路:前口袋、後口袋;跑步:前口袋、後口袋)
	 * @call 讀取完所有位置的動作資料
	 */
	public void writeSelActData(List<float[]> list, String subfolder) {
		isExitFolder(path + subfolder);

		try {
			FileWriter fw = new FileWriter(path + subfolder + "/data.csv",
					false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < list.size(); i++) {
				for (int j = 0; j < 42; j++) {
					bw.write(String.valueOf(list.get(i)[j]));
					if (j < 41)
						bw.write(",");
				}

				bw.newLine();

				if (i % TrainService.LENGTH == TrainService.LENGTH - 1
						&& i != list.size() - 1) {
					bw.newLine();
				}
			}

			bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void isExitFolder(String s) {
		File file = new File(s);

		if (!file.exists()) {
			file.mkdir();
		}
	}

	public boolean isExitFile(String s) {
		File file = new File(s);

		if (!file.exists()) {
			return false;
		}

		return true;
	}

	public void writeWekaData(List<float[]> tmp, int ch) {
		isExitFolder(path + "weka");
		isExitFolder(path + "weka" + "/" + selPos);
		isExitFolder(path + "weka" + "/walk");

		List<float[]> actList = new LinkedList<float[]>();
		actList = tmp;
		actList = Characterization.normalization(actList);

		switch (ch) {
		case 1:
			try {
				FileWriter fw = new FileWriter(path + "weka" + "/" + selPos
						+ "/data_init.csv", false);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write("class");
				bw.write(",");
				for (int i = 0; i < 42; i++) {
					bw.write("f" + String.valueOf(i));

					if (i < 41)
						bw.write(",");
				}

				bw.newLine();

				for (int i = 0; i < actList.size(); i++) {
					bw.write(String.valueOf(i / TrainService.LENGTH) + ",");

					for (int j = 0; j < 42; j++) {
						bw.write(String.valueOf(actList.get(i)[j]));
						if (j < 41)
							bw.write(",");
					}

					if (i != actList.size() - 1) {
						bw.newLine();
					}
				}

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			break;
		case 2:
			try {
				FileWriter fw = new FileWriter(path + "weka" + "/" + selPos
						+ "/data_init_avg.csv", false);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write("class");
				bw.write(",");
				for (int i = 0; i < 42; i++) {
					bw.write("f" + String.valueOf(i));
					if (i < 41)
						bw.write(",");
				}

				bw.newLine();

				int num = actList.size() / TrainService.LENGTH;
				float[][] avgFeature = new float[num][42];
				float sum = 0;

				for (int i = 0; i < num; i++) {
					for (int j = 0; j < 42; j++) {
						sum = 0;
						for (int k = 0; k < TrainService.LENGTH; k++) {
							sum += actList.get(i * TrainService.LENGTH + k)[j];
						}

						avgFeature[i][j] = sum / TrainService.LENGTH;
					}
				}

				for (int i = 0; i < avgFeature.length; i++) {
					bw.write(String.valueOf(i) + ",");

					for (int j = 0; j < 42; j++) {
						bw.write(String.valueOf(avgFeature[i][j]));
						if (j < 41)
							bw.write(",");
					}

					if (i != actList.size() - 1) {
						bw.newLine();
					}
				}

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			break;

		case 3:
			try {
				FileWriter fw = new FileWriter(path + "weka" + "/" + selPos
						+ "/data_s.csv", false);
				BufferedWriter bw = new BufferedWriter(fw);

				String str = ToolKits.getString(this, selPos, null);
				String[] _tmp = str.split(",");
				List<Integer> selFeatureList = new LinkedList<Integer>();

				bw.write("class");
				bw.write(",");

				for (int i = 0; i < _tmp.length; i++) {
					selFeatureList.add(Integer.valueOf(_tmp[i]));
					NodeProc.selFeatureList.add(Integer.valueOf(_tmp[i]));
				}

				for (int i = 0; i < selFeatureList.size(); i++) {
					bw.write("f" + selFeatureList.get(i));
					if (i < selFeatureList.size() - 1)
						bw.write(",");
				}

				bw.newLine();

				for (int i = 0; i < actList.size(); i++) {
					bw.write(String.valueOf(i / TrainService.LENGTH) + ",");

					for (int j = 0; j < selFeatureList.size(); j++) {
						bw.write(String.valueOf(actList.get(i)[selFeatureList
								.get(j)]));
						if (j < selFeatureList.size() - 1)
							bw.write(",");
					}

					if (i != actList.size() - 1) {
						bw.newLine();
					}
				}

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			break;
		case 4:
			try {
				FileWriter fw = new FileWriter(path + "weka" + "/" + selPos
						+ "/data_s_avg.csv", false);
				BufferedWriter bw = new BufferedWriter(fw);

				String str = ToolKits.getString(this, selPos, null);
				String[] _tmp = str.split(",");
				List<Integer> selFeatureList = new LinkedList<Integer>();

				bw.write("class");
				bw.write(",");

				for (int i = 0; i < _tmp.length; i++) {
					selFeatureList.add(Integer.valueOf(_tmp[i]));
					NodeProc.selFeatureList.add(Integer.valueOf(_tmp[i]));
				}

				for (int i = 0; i < selFeatureList.size(); i++) {
					bw.write("f" + selFeatureList.get(i));

					if (i < selFeatureList.size() - 1)
						bw.write(",");
				}

				bw.newLine();

				int num = actList.size() / TrainService.LENGTH;
				float[][] avgFeature = new float[num][selFeatureList.size()];
				float sum = 0;

				for (int i = 0; i < num; i++) {
					for (int j = 0; j < selFeatureList.size(); j++) {
						sum = 0;
						for (int k = 0; k < TrainService.LENGTH; k++) {
							sum += actList.get(i * TrainService.LENGTH + k)[selFeatureList
									.get(j)];
						}

						avgFeature[i][j] = sum / TrainService.LENGTH;
					}
				}

				for (int i = 0; i < avgFeature.length; i++) {
					bw.write(String.valueOf(i) + ",");

					for (int j = 0; j < selFeatureList.size(); j++) {
						bw.write(String.valueOf(avgFeature[i][j]));
						if (j < selFeatureList.size() - 1)
							bw.write(",");
					}

					if (i != actList.size() - 1) {
						bw.newLine();
					}
				}

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			break;
		case 5:
			try {
				FileWriter fw = new FileWriter(path + "weka" + "/walk"
						+ "/data_w_init.csv", false);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write("class");
				bw.write(",");
				for (int i = 0; i < 42; i++) {
					bw.write("f" + String.valueOf(i));

					if (i < 41)
						bw.write(",");
				}

				bw.newLine();

				for (int i = 0; i < walkNorPosList.size(); i++) {
					bw.write(String.valueOf(i / TrainService.LENGTH) + ",");

					for (int j = 0; j < 42; j++) {
						bw.write(String.valueOf(walkNorPosList.get(i)[j]));
						if (j < 41)
							bw.write(",");
					}

					if (i != walkNorPosList.size() - 1) {
						bw.newLine();
					}
				}

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			break;
		case 6:
			try {
				FileWriter fw = new FileWriter(path + "weka" + "/walk"
						+ "/data_w_avg.csv", false);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write("class");
				bw.write(",");
				for (int i = 0; i < 42; i++) {
					bw.write("f" + String.valueOf(i));
					if (i < 41)
						bw.write(",");
				}

				bw.newLine();

				int num = walkNorPosList.size() / TrainService.LENGTH;
				float[][] avgFeature = new float[num][42];
				float sum = 0;

				for (int i = 0; i < num; i++) {
					for (int j = 0; j < 42; j++) {
						sum = 0;
						for (int k = 0; k < TrainService.LENGTH; k++) {
							sum += walkNorPosList.get(i * TrainService.LENGTH
									+ k)[j];
						}

						avgFeature[i][j] = sum / TrainService.LENGTH;
					}
				}

				for (int i = 0; i < avgFeature.length; i++) {
					bw.write(String.valueOf(i) + ",");

					for (int j = 0; j < 42; j++) {
						bw.write(String.valueOf(avgFeature[i][j]));
						if (j < 41)
							bw.write(",");
					}

					if (i != actList.size() - 1) {
						bw.newLine();
					}
				}

				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			break;
		}
	}

	public static boolean isServiceRunning(Context context,
			String serviceClassName) {
		final ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		for (RunningServiceInfo runningServiceInfo : services) {
			if (runningServiceInfo.service.getClassName().equals(
					serviceClassName)) {
				return true;
			}
		}

		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (System.currentTimeMillis() - touchTime > 1500) {
				Toast.makeText(this, "再按一次退出上一頁", Toast.LENGTH_SHORT).show();
				touchTime = System.currentTimeMillis();
			} else {
				finish();
			}

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int index,
			long id) {
		curSelPos = position[index];
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isServiceRunning(this, "com.dsg.recogactivity.service.TrainService")) {
			stopService(new Intent(this, TrainService.class));
		}
	}
}
