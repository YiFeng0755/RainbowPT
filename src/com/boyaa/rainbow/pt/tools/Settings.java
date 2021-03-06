package com.boyaa.rainbow.pt.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Parameters in Setting Activity
 * 
 * 
 * 
 */
public final class Settings {

	public static final String KEY_SENDER = "sender";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_RECIPIENTS = "recipients";
	public static final String KEY_SMTP = "smtp";
	public static final String KEY_ISFLOAT = "isfloat";
	public static final String KEY_INTERVAL = "interval";
	public static final String KEY_ROOT = "root";

	public static SharedPreferences getDefaultSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

}
