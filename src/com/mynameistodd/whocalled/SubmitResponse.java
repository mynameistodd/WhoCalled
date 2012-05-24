package com.mynameistodd.whocalled;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Calendar;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.net.Credentials;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class SubmitResponse extends Activity {

	private String number;
	private Intent callingIntent;
	private Button submitButton;
	private EditText whoCalledInput;
	private EditText callerIDInput;
	private TextView who_called_from;
	private CheckBox checkBox1;
	String baseURL = "http://whocalled.us/do?action=report&name=test&pass=test&phoneNumber=";
	String baseMyURL = "http://strong-robot-8518.herokuapp.com/";
	String baseMyURLJson = baseMyURL + "answers.json";
	String baseMyURLAnswers = baseMyURL + "answers/";
	String resultWCUS;
	String resultWCH;
	AdView adView;
	private Util whoCalledUtil;
	Facebook facebook = new Facebook("206261666149850");
	AsyncFacebookRunner mAsyncRunner;
	private SharedPreferences mPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submit_response);
		
		callingIntent = getIntent();
		whoCalledUtil = new Util(this);
		
		number = callingIntent.getStringExtra("com.mynameistodd.whocalled.unknownNumber");
		submitButton = (Button)findViewById(R.id.button1);
		whoCalledInput = (EditText)findViewById(R.id.whoCalledInput);
		callerIDInput = (EditText)findViewById(R.id.callerIDInput);
		who_called_from = (TextView)findViewById(R.id.textView1);
		adView = (AdView)findViewById(R.id.adView);
		checkBox1 = (CheckBox)findViewById(R.id.checkBox1);
	    
		who_called_from.append(" " + number + "?");
		
	    AdRequest adRequest = new AdRequest();
	    //adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
	    //adRequest.addTestDevice("20758E6052B8B6F7C4F6A3392B9B15E3");
	    adView.loadAd(adRequest);
		
		submitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String returnMessage = "";
				
				returnMessage = postWhoCalledResponses(whoCalledInput.getText().toString(), callerIDInput.getText().toString());
				
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
				
				finish();		
			}
		});
        
		mPrefs = getSharedPreferences(Util.PREFERENCES_NAME, MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
	}

	public String postWhoCalledResponses(String whoCalled, String callerID) 
	{
		String encodedWhoCalled = "";
        String encodedCallerID = "";
		try {
			encodedWhoCalled = URLEncoder.encode(whoCalled, "UTF-8");
			encodedCallerID = URLEncoder.encode(callerID, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
        resultWCUS = postWhoCalledUS(encodedWhoCalled, encodedCallerID);
        resultWCH = postWhoCalledHeroku(encodedWhoCalled, encodedCallerID);
        postFacebook(resultWCH);
        
        whoCalledUtil.saveToCache(number, whoCalled);
        
        return resultWCUS;
    }
	
	public String postWhoCalledUS(String encodedWhoCalled, String encodedCallerID)
	{
		HttpClient httpclient = new DefaultHttpClient();
		ResponseHandler<String> handler = new BasicResponseHandler();
        
        final Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DATE);
        StringBuilder currentDate = new StringBuilder().append(year).append("-").append(month).append("-").append(day);
        
        String postArgs = baseURL + number + "&date=" + currentDate + "&callerID=" + encodedCallerID + "&identity=" + encodedWhoCalled + "&postalCode=";
        HttpPost request = new HttpPost(postArgs);
        try {  
            resultWCUS = httpclient.execute(request, handler);  
            Log.d("mynameistodd", "result: " + resultWCUS);
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        
        httpclient.getConnectionManager().shutdown();
        return resultWCUS;
	}
	
	public String postWhoCalledHeroku(String encodedWhoCalled, String encodedCallerID)
	{
		HttpClient httpclient = new DefaultHttpClient();
		ResponseHandler<String> handler = new BasicResponseHandler();
		HttpPost request = new HttpPost(baseMyURLJson);
		
//		DefaultHttpClient client = new DefaultHttpClient();
//		Credentials creds = new UsernamePasswordCredentials("todd", "supersecret");
//		client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
        
        JSONObject answer = new JSONObject();
        try {
			answer.put("title", encodedWhoCalled);
			answer.put("phone", number);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
        
        StringEntity s = null;
		try {
			s = new StringEntity(answer.toString());
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        s.setContentType("application/json");
        
        request.setEntity(s);
        
        try {  
            resultWCH = httpclient.execute(request, handler);  
            Log.d("mynameistodd", "result2: " + resultWCH);
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        
        httpclient.getConnectionManager().shutdown();
        return resultWCH;
	}
	
	public void postFacebook(String herokuResponse)
	{
		Log.d("mynameistodd", "Facebook isSessionValid: " + String.valueOf(facebook.isSessionValid()));
		boolean postToFacebook = checkBox1.isChecked();
		
        if (facebook.isSessionValid() && postToFacebook) {
        	
        	String responseID = "";
        	try {
				JSONObject response = new JSONObject(herokuResponse);
				responseID = response.getString("id");
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
        	
        	mAsyncRunner = new AsyncFacebookRunner(facebook);
        	Bundle parameters = new Bundle();
        	parameters.putString("access_token", facebook.getAccessToken());
        	parameters.putString("answer", baseMyURLAnswers + responseID);
        	mAsyncRunner.request("me/com_mynameistodd_wc:submit", parameters, "POST", new RequestListener() {
				
				@Override
				public void onMalformedURLException(MalformedURLException e, Object state) {
					e.printStackTrace();
				}
				
				@Override
				public void onIOException(IOException e, Object state) {
					e.printStackTrace();
				}
				
				@Override
				public void onFileNotFoundException(FileNotFoundException e, Object state) {
					e.printStackTrace();
				}
				
				@Override
				public void onFacebookError(FacebookError e, Object state) {
					e.printStackTrace();
				}
				
				@Override
				public void onComplete(String response, Object state) {
					Log.d("mynameistodd", response);
				}
			}, null);
        }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		adView.destroy();
	}
}
