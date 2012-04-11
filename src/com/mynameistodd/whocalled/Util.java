package com.mynameistodd.whocalled;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Util {
	Context curContext;
	String PREFERENCES_NAME = "WhoCalled";
	String baseURL = "http://whocalled.us/do?action=getWho&name=test&pass=test&phoneNumber=";
	
	public Util(Context curContext) {
		super();
		this.curContext = curContext;
	}

	public String getWhoCalledResponse(String phoneNumber){
		String result = "";
        HttpClient httpclient = new DefaultHttpClient();  
        HttpGet request = new HttpGet(baseURL + phoneNumber);
        ResponseHandler<String> handler = new BasicResponseHandler();  
        try {  
            result = httpclient.execute(request, handler);  
            Log.d("mynameistodd", "result: " + result);
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        httpclient.getConnectionManager().shutdown();
        
        String[] splitQueryString = new String[0];
        String[] whoKeyVal = new String[0];
        String who = "Unknown Caller";
        if (result.length() > 0)
        {
	        splitQueryString = result.split("&");
	        if (splitQueryString.length > 1)
	        {
	        	whoKeyVal = splitQueryString[1].split("=");
	        }
	        if (whoKeyVal.length > 1)
	        {
	        	who = whoKeyVal[1];
	        }
	        if (who != "Unknown Caller")
	        {
	        	saveToCache(phoneNumber, who);
	        }
        }
        
        return who;
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
