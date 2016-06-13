package com.dsg.recogactivity.utils;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.dsg.recogactivity.R;

public class PromptVoice {
	private SoundPool mSoundPool1;
	private SoundPool mSoundPool2;
	private SoundPool mSoundPool3;
	private SoundPool mSoundPool4;
	private SoundPool mSoundPool5;
	private SoundPool mSoundPool6;
	private SoundPool mSoundPool7;
	
	private HashMap<Integer, Integer> soundMap1 = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> soundMap2 = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> soundMap3 = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> soundMap4 = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> soundMap5 = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> soundMap6 = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> soundMap7 = new HashMap<Integer, Integer>();
	
	private Context context;
	
	public PromptVoice(Context context) {
		this.context = context;
		
		// 倒數計時聲(1 to 22)
		mSoundPool1 = new SoundPool(22, AudioManager.STREAM_MUSIC, 0);
		// 開始提醒聲(1 to 22)
		mSoundPool2 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		// 動作提醒聲
		mSoundPool3 = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
		// 放置手機提醒聲
		mSoundPool4 = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);	
		// 走路(測試模式)
		mSoundPool5 = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		// 下樓梯(測試模式)
		mSoundPool6 = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		// 上樓梯(測試模式)
		mSoundPool7 = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);	
		
		soundMap1.put(1, mSoundPool1.load(this.context, R.raw.a1, 1));
		soundMap1.put(2, mSoundPool1.load(this.context, R.raw.a2, 1));
		soundMap1.put(3, mSoundPool1.load(this.context, R.raw.a3, 1));
		soundMap1.put(4, mSoundPool1.load(this.context, R.raw.a4, 1));
		soundMap1.put(5, mSoundPool1.load(this.context, R.raw.a5, 1));
		soundMap1.put(6, mSoundPool1.load(this.context, R.raw.a6, 1));
		soundMap1.put(7, mSoundPool1.load(this.context, R.raw.a7, 1));
		soundMap1.put(8, mSoundPool1.load(this.context, R.raw.a8, 1));
		soundMap1.put(9, mSoundPool1.load(this.context, R.raw.a9, 1));
		soundMap1.put(10, mSoundPool1.load(this.context, R.raw.a10, 1));
		soundMap1.put(11, mSoundPool1.load(this.context, R.raw.a11, 1));
		soundMap1.put(12, mSoundPool1.load(this.context, R.raw.a12, 1));
		soundMap1.put(13, mSoundPool1.load(this.context, R.raw.a13, 1));
		soundMap1.put(14, mSoundPool1.load(this.context, R.raw.a14, 1));
		soundMap1.put(15, mSoundPool1.load(this.context, R.raw.a15, 1));
		soundMap1.put(16, mSoundPool1.load(this.context, R.raw.a16, 1));
		soundMap1.put(17, mSoundPool1.load(this.context, R.raw.a17, 1));
		soundMap1.put(18, mSoundPool1.load(this.context, R.raw.a18, 1));
		soundMap1.put(19, mSoundPool1.load(this.context, R.raw.a19, 1));
		soundMap1.put(20, mSoundPool1.load(this.context, R.raw.a20, 1));
		soundMap1.put(21, mSoundPool1.load(this.context, R.raw.a21, 1));
		soundMap1.put(22, mSoundPool1.load(this.context, R.raw.a22, 1));
			
		soundMap2.put(1, mSoundPool2.load(this.context, R.raw.begin, 1));
		
		soundMap3.put(1, mSoundPool3.load(this.context, R.raw.change_sit, 1));
		soundMap3.put(2, mSoundPool3.load(this.context, R.raw.change_stand, 1));
		soundMap3.put(3, mSoundPool3.load(this.context, R.raw.change_walk, 1));
		soundMap3.put(4, mSoundPool3.load(this.context, R.raw.change_descend, 1));
		soundMap3.put(5, mSoundPool3.load(this.context, R.raw.change_ascend, 1));
		soundMap3.put(6, mSoundPool3.load(this.context, R.raw.change_run, 1));	
		
		soundMap4.put(1, mSoundPool4.load(this.context, R.raw.placement, 1));
		
		soundMap5.put(1, mSoundPool5.load(this.context, R.raw.walk_first, 1));
		soundMap5.put(2, mSoundPool5.load(this.context, R.raw.walk_second, 1));
		soundMap5.put(3, mSoundPool5.load(this.context, R.raw.walk_third, 1));
		soundMap5.put(4, mSoundPool5.load(this.context, R.raw.walk_forth, 1));
		soundMap5.put(5, mSoundPool5.load(this.context, R.raw.walk_fifth, 1));		
		
		soundMap6.put(1, mSoundPool6.load(this.context, R.raw.descend_first, 1));
		soundMap6.put(2, mSoundPool6.load(this.context, R.raw.descend_second, 1));
		soundMap6.put(3, mSoundPool6.load(this.context, R.raw.descend_third, 1));
		soundMap6.put(4, mSoundPool6.load(this.context, R.raw.descend_forth, 1));
		soundMap6.put(5, mSoundPool6.load(this.context, R.raw.descend_fifth, 1));	
		
		soundMap7.put(1, mSoundPool7.load(this.context, R.raw.ascend_first, 1));
		soundMap7.put(2, mSoundPool7.load(this.context, R.raw.ascend_second, 1));
		soundMap7.put(3, mSoundPool7.load(this.context, R.raw.ascend_third, 1));
		soundMap7.put(4, mSoundPool7.load(this.context, R.raw.ascend_forth, 1));
		soundMap7.put(5, mSoundPool7.load(this.context, R.raw.ascend_fifth, 1));	
	}
	
	public void playCountdown (int position) {
		mSoundPool1.play(soundMap1.get(position), 1, 1, 0, 0, 1); 
	}
	
	public void playBegin() {
		mSoundPool2.play(soundMap2.get(1), 1, 1, 0, 0, 1); 
	}
	
	public void playChangeAct(int position) {
		mSoundPool3.play(soundMap3.get(position), 1, 1, 0, 0, 1); 
	}	
	
	public void playPlacement () {
		mSoundPool4.play(soundMap4.get(1), 1, 1, 0, 0, 1); 
	}
	
	public void playWalkTestCountdown (int position) {
		mSoundPool5.play(soundMap5.get(position), 1, 1, 0, 0, 1); 
	}
	
	public void playDescendTestCountdown (int position) {
		mSoundPool6.play(soundMap6.get(position), 1, 1, 0, 0, 1); 
	}
	
	public void playAscendTestCountdown (int position) {
		mSoundPool7.play(soundMap7.get(position), 1, 1, 0, 0, 1); 
	}		
}
