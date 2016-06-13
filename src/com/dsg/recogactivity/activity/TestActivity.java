package com.dsg.recogactivity.activity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dsg.recogactivity.R;
import com.dsg.recogactivity.constant.MessageDef;
import com.dsg.recogactivity.object.ActInfo;
import com.dsg.recogactivity.service.TestService;
import com.dsg.recogactivity.service.TrainService;
import com.dsg.recogactivity.utils.RecordPosAct;
import com.dsg.recogactivity.utils.ToolKits;

public class TestActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener, OnItemSelectedListener {
	private TextView tvActInfo, tvPosInfo;
	private TextView tvSitTime, tvStandTime, tvWalkTime, tvRunTime,
			tvAscendTime, tvDescendTime;
	private Button btnStart, btnStop, btnSave;
	private RadioGroup rgOption;
	private ListView lvActInfo;
	private Spinner spnPos;
	private ImageView ivOption;

	/** service communicate with activity */
	public static Handler mHandler;

	public static String curSelPos = "";

	private List<ActInfo> actInfoList = new LinkedList<ActInfo>();
	private MyAdapter adapter;

	private int[] time = new int[6];

	private String[] position;
	
	private long touchTime = 0;

	private int option = 5; // initial value

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		/** remain screen turned on */
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		actInfoList.add(new ActInfo("Activity", "Time"));

		initView();
		initHandler();
	}

	public void initView() {
		tvActInfo = (TextView) findViewById(R.id.tv_act_info);
		tvPosInfo = (TextView) findViewById(R.id.tv_pos_info);

		tvSitTime = (TextView) findViewById(R.id.tv_sit_time);
		tvStandTime = (TextView) findViewById(R.id.tv_stand_time);
		tvWalkTime = (TextView) findViewById(R.id.tv_walk_time);
		tvRunTime = (TextView) findViewById(R.id.tv_run_time);
		tvAscendTime = (TextView) findViewById(R.id.tv_ascend_time);
		tvDescendTime = (TextView) findViewById(R.id.tv_descend_time);

		btnStart = (Button) findViewById(R.id.btn_start);
		btnStop = (Button) findViewById(R.id.btn_stop);
		btnSave = (Button) findViewById(R.id.btn_save);

		rgOption = (RadioGroup) findViewById(R.id.rg_option);

		lvActInfo = (ListView) findViewById(R.id.lv_actinfo);

		spnPos = (Spinner) findViewById(R.id.spn_pos);

		ivOption = (ImageView) findViewById(R.id.iv_option);

		btnStart.setOnClickListener(this);
		btnStop.setOnClickListener(this);
		btnSave.setOnClickListener(this);

		rgOption.setOnCheckedChangeListener(this);

		adapter = new MyAdapter();
		lvActInfo.setAdapter(adapter);

		getSelPos();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, position);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		spnPos.setAdapter(adapter);
		spnPos.setOnItemSelectedListener(this);

		ivOption.setOnClickListener(this);

		tvSitTime.setText("0");
		tvStandTime.setText("0");
		tvWalkTime.setText("0");
		tvRunTime.setText("0");
		tvAscendTime.setText("0");
		tvDescendTime.setText("0");
	}

	public void initHandler() {
		mHandler = new Handler() {
			/**
			 * @des 更新使用者位置及動作資料
			 * @call 當位置識別或動作識別的判斷完成
			 */
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MessageDef.UPDATA_POS:
					tvPosInfo.setText("");
					tvPosInfo.setText(RecordPosAct.recordPos);
					break;
				case MessageDef.UPDATA_ACT:
					String str = "";

					for (int i = 0; i < RecordPosAct.recordActList.size(); i++) {
						str = str + RecordPosAct.recordActList.get(i) + ",";
					}

					str = str.substring(0, str.length() - 1);

					calActTime(RecordPosAct.recordActList
							.get(RecordPosAct.recordActList.size() - 1));

					SimpleDateFormat formatter = new SimpleDateFormat(
							"HH:mm:ss");
					Date curDate = new Date(System.currentTimeMillis());
					String time = formatter.format(curDate);

					actInfoList.add(new ActInfo(RecordPosAct.recordActList
							.get(RecordPosAct.recordActList.size() - 1), time));
					adapter.notifyDataSetChanged();

					tvActInfo.setText("");
					tvActInfo.setText(str);
					break;
				case MessageDef.UPDATA_BUTTON_STATUS: // 當一系列動作訓練完成時，改成按鈕的文字
					btnStart.setEnabled(true);
					break;
				}

				super.handleMessage(msg);
			}
		};
	}

	/**
	 * @dsc 得到spinner所需要的所有使用者手機擺放位置
	 * @call 在spinner填充資料前
	 */
	public void getSelPos() {
		String str = ToolKits.getString(this,
				PositionActivity.USER_SELECT_POSITION, null);
		position = str.split(",");
	}

	/**
	 * @dsc 計算動作"累積"的時間
	 * @call 產生動作
	 */
	public void calActTime(String act) {
		if (act.equals("Sit")) {
			tvSitTime.setText("");
			tvSitTime.setText(String.valueOf(++time[0]));
		} else if (act.equals("Stand")) {
			tvStandTime.setText("");
			tvStandTime.setText(String.valueOf(++time[1]));
		} else if (act.equals("Walk")) {
			tvWalkTime.setText("");
			tvWalkTime.setText(String.valueOf(++time[2]));
		} else if (act.equals("Run")) {
			tvRunTime.setText("");
			tvRunTime.setText(String.valueOf(++time[3]));
		} else if (act.equals("AscendStair")) {
			tvAscendTime.setText("");
			tvAscendTime.setText(String.valueOf(++time[4]));
		} else if (act.equals("DescendStair")) {
			tvDescendTime.setText("");
			tvDescendTime.setText(String.valueOf(++time[5]));
		} else if (act.equals("")) {
			tvSitTime.setText("0");
			tvStandTime.setText("0");
			tvWalkTime.setText("0");
			tvRunTime.setText("0");
			tvAscendTime.setText("0");
			tvDescendTime.setText("0");
		}
	}

	@Override
	public void onClick(View v) {
		Intent mIntent = new Intent(this, TestService.class);

		switch (v.getId()) {
		case R.id.btn_start:

			if (option != 0) {
				mIntent.putExtra("option", option);
				startService(mIntent);

				tvPosInfo.setText("");
				tvActInfo.setText("");

				RecordPosAct.recordPos = "";
				RecordPosAct.recordActList.clear();

				actInfoList.clear();
				adapter.notifyDataSetChanged();

				btnStart.setEnabled(false);

				for (int i = 0; i < 6; i++)
					time[i] = 0;

				calActTime("");

			} else {
				Toast.makeText(this, "請挑選你要測試的項目!!!", Toast.LENGTH_SHORT)
						.show();
			}

			break;
		case R.id.btn_stop:
			btnStart.setEnabled(true);

			if (TestService.mTimer != null)
				TestService.mTimer.cancel();

			if (isServiceRunning(this,
					"com.dsg.recogactivity.service.TestService")) {
				stopService(new Intent(this, TestService.class));
			}

			break;
		case R.id.btn_save:
//			saveFeedbackData();

			break;
		case R.id.iv_option: // 切換至設置頁面
			Intent intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
			break;
		}
	}

	// sit;stand;walk;descend;ascend;run
	public void saveFeedbackData() {
		List<float[]> list = new LinkedList<float[]>();

		for (int i = 0; i < TrainService.LENGTH; i++)
			list.add(TestService.sitList.get(i).getFeature().getFeatures());

		for (int i = 0; i < TrainService.LENGTH; i++)
			list.add(TestService.standList.get(i).getFeature().getFeatures());

		for (int i = 0; i < TrainService.LENGTH; i++)
			list.add(TestService.walkList.get(i).getFeature().getFeatures());

		for (int i = 0; i < TrainService.LENGTH; i++)
			list.add(TestService.descendStairList.get(i).getFeature()
					.getFeatures());

		for (int i = 0; i < TrainService.LENGTH; i++)
			list.add(TestService.ascendStairList.get(i).getFeature()
					.getFeatures());

		for (int i = 0; i < TrainService.LENGTH; i++)
			list.add(TestService.runList.get(i).getFeature().getFeatures());

		writeFeedbackData(list, Environment.getExternalStorageDirectory()
				.getPath() + "/TrainData/" + "/data_act.csv");
	}

	public void writeFeedbackData(List<float[]> list, String filePath) {
		try {
			FileWriter fw = new FileWriter(filePath, false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < 6 * TrainService.LENGTH; i++) {
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

	public class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return actInfoList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;

			if (convertView == null) {
				LayoutInflater inflater = TestActivity.this.getLayoutInflater();
				view = inflater.inflate(R.layout.list_item, null);
			} else {
				view = convertView;
			}

			TextView tvAct = (TextView) view.findViewById(R.id.tv_act);
			TextView tvTime = (TextView) view.findViewById(R.id.tv_time);

			ActInfo actInfo = actInfoList.get(position);

			tvAct.setText(actInfo.getAction());
			tvTime.setText(actInfo.getTime());

			return view;
		}
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
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rb_option1:
			option = 1;
			break;
		case R.id.rb_option2:
			option = 2;
			break;
		case R.id.rb_option3:
			option = 3;
			break;
		case R.id.rb_option4:
			option = 4;
			break;
		case R.id.rb_option5:
			option = 5;
			break;
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

		if (isServiceRunning(this, "com.dsg.recogactivity.service.TestService")) {
			stopService(new Intent(this, TestService.class));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
