package com.mynameistodd.whocalled;

import android.content.Context;
import android.content.SharedPreferences;

public class Util {
	Context curContext;
	public static String PREFERENCES_NAME = "WhoCalled";
	
	public Util(Context curContext) {
		super();
		this.curContext = curContext;
	}

	public boolean saveToCache(String number, String name)
	{
		SharedPreferences settings = curContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString(number, name);
	    return editor.commit();
	}
	
	public String readFromCache(String number)
	{
		SharedPreferences settings = curContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
	    return settings.getString(number, "Unknown Caller");
	}
}
