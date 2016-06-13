package com.dsg.recogactivity.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.dsg.recogactivity.R;
import com.dsg.recogactivity.utils.ToolKits;

public class SettingActivity extends Activity implements OnCheckedChangeListener, OnClickListener {
	private CheckBox cbSimpleFeature;
	private CheckBox cbFeedback;
	private CheckBox cbSVM;
	private CheckBox cbSelectedPosition;
	private Button btnSaveOption;
	
	private int isUseSimpleFeature = 0;
	private int isUseFeedback = 0;
	private int isUseSVM = 0;
	private int isUseSelectedPosition = 0;
	
	public static final String IS_USE_SIMPLE_FEATURE = "is_use_simple_feature";
	public static final String IS_USE_FEEDBACK = "is_use_feedback";
	public static final String IS_USE_SVM = "is_use_svm";
	public static final String IS_USE_SELECTED_POSITION= "is_use_selected_position";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		initView();
	}
	
	public void initView() {	
		cbSimpleFeature = (CheckBox) findViewById(R.id.cb_simple_feature);
		cbFeedback = (CheckBox) findViewById(R.id.cb_feedback);
		cbSVM = (CheckBox) findViewById(R.id.cb_svm);
		cbSelectedPosition = (CheckBox) findViewById(R.id.cb_Selected_position);
		
		btnSaveOption = (Button) findViewById(R.id.btn_save_option);
		
		cbSimpleFeature.setOnCheckedChangeListener(this);
		cbFeedback.setOnCheckedChangeListener(this);
		cbSVM.setOnCheckedChangeListener(this);
		cbSelectedPosition.setOnCheckedChangeListener(this);
		
		if(ToolKits.getInteger(this, IS_USE_SIMPLE_FEATURE, 0) == 0) {
			cbSimpleFeature.setChecked(false);
		} else {
			cbSimpleFeature.setChecked(true);
		}
		
		if(ToolKits.getInteger(this, IS_USE_FEEDBACK, 0) == 0) {
			cbFeedback.setChecked(false);
		} else {
			cbFeedback.setChecked(true);
		}
		
		if(ToolKits.getInteger(this, IS_USE_SVM, 0) == 0) {
			cbSVM.setChecked(false);
		} else {
			cbSVM.setChecked(true);
		}
		
		if(ToolKits.getInteger(this, IS_USE_SELECTED_POSITION, 0) == 0) {
			cbSelectedPosition.setChecked(false);
		} else {
			cbSelectedPosition.setChecked(true);
		}
		
		btnSaveOption.setOnClickListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		if(cbSimpleFeature.isChecked()) {
			isUseSimpleFeature = 1;
		} else {
			isUseSimpleFeature = 0;
		}
		
		if(cbFeedback.isChecked()) {
			isUseFeedback = 1;
		} else {
			isUseFeedback = 0;
		}
		
		if(cbSVM.isChecked()) {
			isUseSVM = 1;
		} else {
			isUseSVM = 0;
		}
		
		if(cbSelectedPosition.isChecked()) {
			isUseSelectedPosition = 1;
		} else {
			isUseSelectedPosition = 0;
		}
	}

	@Override
	public void onClick(View arg0) {
		ToolKits.putInteger(this, IS_USE_SIMPLE_FEATURE, isUseSimpleFeature);
		ToolKits.putInteger(this, IS_USE_FEEDBACK, isUseFeedback);
		ToolKits.putInteger(this, IS_USE_SVM, isUseSVM);
		ToolKits.putInteger(this, IS_USE_SELECTED_POSITION, isUseSelectedPosition);		
		
		Intent intent = new Intent(this, TestActivity.class);
		startActivity(intent);		
		
		finish();
	}
}
