package com.dsg.recogactivity.activity;

import com.dsg.recogactivity.R;
import com.dsg.recogactivity.utils.ToolKits;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class PositionActivity extends Activity implements OnClickListener {
	private CheckBox ckb1, ckb2, ckb3, ckb4, ckb5;
	private Button btnOK;
			
	public static final String IS_OPEN_SELECT_POSITION_ACTIVITY = "is_open_select_position_activity";
	public static final String USER_SELECT_POSITION = "user_select_position";
	
	private long touchTime = 0;
	
	private String userSelPos;
		
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(ToolKits.getBoolean(this, IS_OPEN_SELECT_POSITION_ACTIVITY, false)) {	
			Intent intent = new Intent(this, TrainActivity.class);
			startActivity(intent);
					
			finish();		
		} else {							
			setContentView(R.layout.activity_postitoin);
			initView();	
		}
	}

	private void initView() {
		ckb1 = (CheckBox)findViewById(R.id.ckb1);
		ckb2 = (CheckBox)findViewById(R.id.ckb2);
		ckb3 = (CheckBox)findViewById(R.id.ckb3);
		ckb4 = (CheckBox)findViewById(R.id.ckb4);
		ckb5 = (CheckBox)findViewById(R.id.ckb5);
		
		btnOK = (Button)findViewById(R.id.btn_ok);
		btnOK.setOnClickListener(this);	
	}

	@Override
	public void onClick(View arg0) {
		userSelPos = "";
		
		if(ckb1.isChecked()) {
			userSelPos = userSelPos + ckb1.getText().toString() + ",";
		}
		
		if(ckb2.isChecked()) {
			userSelPos = userSelPos + ckb2.getText().toString() + ",";
		}		
		
		if(ckb3.isChecked()) {
			userSelPos = userSelPos + ckb3.getText().toString() + ",";
		}
		
		if(ckb4.isChecked()) {
			userSelPos = userSelPos + ckb4.getText().toString() + ",";
		}
		
		if(ckb5.isChecked()) {
			userSelPos = userSelPos + ckb5.getText().toString() + ",";
		}	
		
		if(userSelPos.equals("")) {
			Toast.makeText(this, "You not choose any position yet.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		// 刪除最後多餘的字元(,)
		userSelPos = userSelPos.substring(0, userSelPos.length() - 1); 
		
		ToolKits.putBoolean(this, IS_OPEN_SELECT_POSITION_ACTIVITY, true);
		ToolKits.putString(this, USER_SELECT_POSITION, userSelPos);
		
		Intent intent = new Intent(this, TrainActivity.class);
		startActivity(intent);
				
		finish();
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
}
