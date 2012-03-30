package com.mynameistodd.whocalled;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SubmitResponse extends Activity {

	private String number;
	private Intent callingIntent;
	private Button submitButton;
	private EditText whoCalledInput;
	private EditText callerIDInput;
	private TextView who_called_from;
	String baseURL = "http://whocalled.us/do?action=report&name=test&pass=test&phoneNumber=";
	String result;
	AdView adView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submit_response);
		
		callingIntent = getIntent();
		
		number = callingIntent.getStringExtra("com.mynameistodd.whocalled.unknownNumber");
		submitButton = (Button)findViewById(R.id.button1);
		whoCalledInput = (EditText)findViewById(R.id.whoCalledInput);
		callerIDInput = (EditText)findViewById(R.id.callerIDInput);
		who_called_from = (TextView)findViewById(R.id.textView1);
		adView = (AdView)findViewById(R.id.adView);
	    
		who_called_from.append(" " + number + "?");
		
	    AdRequest adRequest = new AdRequest();
	    //adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
	    //adRequest.addTestDevice("20758E6052B8B6F7C4F6A3392B9B15E3");
	    adView.loadAd(adRequest);
		
		submitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String returnMessage = "";
				try {
					returnMessage = postWhoCalledResponse(whoCalledInput.getText().toString(), callerIDInput.getText().toString());
					
					if (returnMessage.equals("success=1"))
					{
						setResult(RESULT_OK);
					}
					else
					{
						Intent returnIntent = new Intent();
						if (returnMessage.length() > 0)
						{
							String[] split1 = returnMessage.split("&");
							if (split1.length > 1)
							{
								String[] split2 = split1[1].split("=");
								if (split2.length > 1)
								{
									returnIntent.putExtra("com.mynameistodd.whocalled.response", split2[1]);
									setResult(2, returnIntent);
								}
								else
								{
									setResult(RESULT_CANCELED);
								}
							}
							else
							{
								setResult(RESULT_CANCELED);
							}
						}
						else
						{
							setResult(RESULT_CANCELED);
						}
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				finish();		
			}
		});
        
		
	}

	public String postWhoCalledResponse(String whoCalled, String callerID) throws UnsupportedEncodingException{  
        HttpClient httpclient = new DefaultHttpClient();
        String encodedWhoCalled = URLEncoder.encode(whoCalled, "UTF-8");
        String encodedCallerID = URLEncoder.encode(callerID, "UTF-8");
        
        final Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DATE);
        StringBuilder currentDate = new StringBuilder().append(year).append("-").append(month).append("-").append(day);
        
        String postArgs = baseURL + number + "&date=" + currentDate + "&callerID=" + encodedCallerID + "&identity=" + encodedWhoCalled + "&postalCode=";
        HttpPost request = new HttpPost(postArgs);
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
        
        saveToCache(number, whoCalled);
        
        return result;
    }
	
	public void saveToCache(String number, String name)
	{
		SharedPreferences settings = getSharedPreferences(MissedCallsList.PREFERENCES_NAME, MODE_PRIVATE);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString(number, name);
	    editor.commit();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		adView.destroy();
	}
}
