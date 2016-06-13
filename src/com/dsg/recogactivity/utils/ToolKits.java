package com.dsg.recogactivity.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ToolKits {
	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences("com.dsg.recogactivity", Context.MODE_PRIVATE);
	}
		
	public static void putString(Context context, String key, String value) {
		SharedPreferences mSharePreferences = getSharedPreferences(context);
		SharedPreferences.Editor editor = mSharePreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static String getString(Context context, String key, String value) {
		SharedPreferences mSharePreferences = getSharedPreferences(context);
		return mSharePreferences.getString(key, value);
	}
	
	public static void putBoolean(Context context, String key, boolean value) {
		SharedPreferences mSharePreferences = getSharedPreferences(context);
		SharedPreferences.Editor editor = mSharePreferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public static boolean getBoolean(Context context, String key, boolean value) {
		SharedPreferences mSharePreferences = getSharedPreferences(context);
		return mSharePreferences.getBoolean(key, value);
	}
	
	public static void putInteger(Context context, String key, int value) {
		SharedPreferences mSharePreferences = getSharedPreferences(context);
		SharedPreferences.Editor editor = mSharePreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public static int getInteger(Context context, String key, int value) {
		SharedPreferences mSharePreferences = getSharedPreferences(context);
		return mSharePreferences.getInt(key, value);
	}
}
