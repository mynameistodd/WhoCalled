package com.mynameistodd.whocalled;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class WhoCalledTabWidget extends TabActivity {
	AdView adView;
	
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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		adView.destroy();
	}
}
