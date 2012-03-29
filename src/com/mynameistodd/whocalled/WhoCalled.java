package com.mynameistodd.whocalled;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WhoCalled extends Activity {

	TextView callerName;
	TextView callerNumber;
	Button reportButton;
	PhoneStateListener listener;
	String result = "";
	String baseURL = "http://whocalled.us/do?action=getWho&name=test&pass=test&phoneNumber=";
	String phoneNumber;
	Intent callingIntent;
	Context curContext;
	GoogleAnalyticsTracker tracker;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        callingIntent = getIntent();
        curContext = getApplicationContext();
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession("UA-26489424-1", this);
        
        Log.d("mynameistodd", "onCreate called.");
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("mynameistodd", "onStarted called.");
        
        callerName = (TextView)findViewById(R.id.TextViewName);
        callerNumber = (TextView)findViewById(R.id.TextViewNumber);
                
        phoneNumber = callingIntent.getStringExtra("com.mynameistodd.whocalled.unknownNumber");
        
        Log.d("mynameistodd", "CI - phoneNumber: " + phoneNumber);
        
        getWhoCalledResponse();
        String[] splitQueryString = new String[0];
        String[] whoKeyVal = new String[0];
        String who = "Unknown";
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

        }    
        callerName.setText(who);
        callerNumber.setText(phoneNumber);
        
        reportButton = (Button)findViewById(R.id.button1);
        reportButton.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(curContext, SubmitResponse.class);
				intent.putExtra("com.mynameistodd.whocalled.unknownNumber", phoneNumber);
				startActivityForResult(intent, 1);
			}
		});
        tracker.trackPageView("/whoCalled/display/"+phoneNumber);
    }
        
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == RESULT_OK)
		{
			Toast.makeText(curContext, "Submitted!", Toast.LENGTH_SHORT).show();
			tracker.trackPageView("/whoCalled/submited/"+phoneNumber);
		}
		else
		{
			String response = "Error: ";
			if (data != null)
			{
				response += data.getStringExtra("com.mynameistodd.whocalled.response");
			}
			Toast.makeText(curContext, response, Toast.LENGTH_LONG).show();
			tracker.trackPageView("/whoCalled/error/"+phoneNumber);
		}
	}
    
    public void getWhoCalledResponse(){  
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
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        tracker.dispatch();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	tracker.stopSession();
    }
}