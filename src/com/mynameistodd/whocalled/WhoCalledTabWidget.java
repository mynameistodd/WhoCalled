package com.mynameistodd.whocalled;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

public class WhoCalledTabWidget extends TabActivity {
	AdView adView;
	private SharedPreferences mPrefs;
	Facebook facebook = new Facebook("206261666149850");
	AsyncFacebookRunner mAsyncRunner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.tabs_list);
	    
	    adView = (AdView)findViewById(R.id.adView);
	    
	    AdRequest adRequest = new AdRequest();
	    //adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
	    //adRequest.addTestDevice("20758E6052B8B6F7C4F6A3392B9B15E3");
	    adView.loadAd(adRequest);
	    
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    //missed calls
	    intent = new Intent().setClass(this, MissedCallsList.class);
	    intent.putExtra("com.mynameistodd.whocalled.calllist", 1);
	    spec = tabHost.newTabSpec("missed").setIndicator("Missed").setContent(intent);
	    tabHost.addTab(spec);
	    
	    //outgoing calls
	    intent = new Intent().setClass(this, MissedCallsList.class);
	    intent.putExtra("com.mynameistodd.whocalled.calllist", 2);
	    spec = tabHost.newTabSpec("outgoing").setIndicator("Outgoing").setContent(intent);
	    tabHost.addTab(spec);
	    
	    //incoming calls
	    intent = new Intent().setClass(this, MissedCallsList.class);
	    intent.putExtra("com.mynameistodd.whocalled.calllist", 3);
	    spec = tabHost.newTabSpec("incoming").setIndicator("Incoming").setContent(intent);
	    tabHost.addTab(spec);
	    
	    tabHost.setCurrentTab(0);
	    
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
			case R.id.auth_fb_item:
				boolean fbSessionValid = facebook.isSessionValid();
				if(!fbSessionValid) {
					facebook.authorize(this, new String[] { "publish_actions" }, new DialogListener() {
						@Override
						public void onComplete(Bundle values) {
							Log.d("mynameistodd", "inside onComplete");
							
							SharedPreferences.Editor editor = mPrefs.edit();
		                    editor.putString("access_token", facebook.getAccessToken());
		                    editor.putLong("access_expires", facebook.getAccessExpires());
		                    editor.commit();
		                    Toast.makeText(getApplicationContext(), "Authorized!", Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onFacebookError(FacebookError error) {
							Log.d("mynameistodd", "inside onFacebookError");
							Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
						}

						@Override
						public void onError(DialogError e) {
							Log.d("mynameistodd", "inside onError");
							Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
						}

						@Override
						public void onCancel() {
							Log.d("mynameistodd", "inside onCancel");
							Toast.makeText(getApplicationContext(), "I'm sad...authorization aborted!", Toast.LENGTH_LONG).show();
						}
					});
				}
				return true;
			case R.id.logout_fb_item:
				mAsyncRunner = new AsyncFacebookRunner(facebook);
				mAsyncRunner.logout(getApplicationContext(), new RequestListener() {
					  @Override
					  public void onComplete(String response, Object state) {
						  Log.d("mynameistodd", "inside onComplete");
						  //Toast.makeText(getApplicationContext(), "Logged Out!", Toast.LENGTH_SHORT).show();
					  }
					  
					  @Override
					  public void onIOException(IOException e, Object state) {
						  Log.d("mynameistodd", "inside onIOException");
					  }
					  
					  @Override
					  public void onFileNotFoundException(FileNotFoundException e,
					        Object state) {
						  Log.d("mynameistodd", "inside onFileNotFoundException");
					  }
					  
					  @Override
					  public void onMalformedURLException(MalformedURLException e,
					        Object state) {
						  Log.d("mynameistodd", "inside onMalformedURLException");
					  }
					  
					  @Override
					  public void onFacebookError(FacebookError e, Object state) {
						  Log.d("mynameistodd", "inside onFacebookError");
					  }
					});
				Toast.makeText(getApplicationContext(), "Logged Out!", Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);		
		}
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }
	
	public void onResume() {    
        super.onResume();
        facebook.extendAccessTokenIfNeeded(this, null);
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		adView.destroy();
	}
}
