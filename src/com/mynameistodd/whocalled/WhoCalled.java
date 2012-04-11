package com.mynameistodd.whocalled;

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
	String phoneNumber;
	Intent callingIntent;
	Context curContext;
	GoogleAnalyticsTracker tracker;
	private Util whoCalledUtil;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        callingIntent = getIntent();
        curContext = getApplicationContext();
        whoCalledUtil = new Util(curContext);
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
        reportButton = (Button)findViewById(R.id.button1);
                
        phoneNumber = callingIntent.getStringExtra("com.mynameistodd.whocalled.unknownNumber");
        
        Log.d("mynameistodd", "CI - phoneNumber: " + phoneNumber);
        
        result = whoCalledUtil.getWhoCalledResponse(phoneNumber);

        callerName.setText(result);
        callerNumber.setText(phoneNumber);
        
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