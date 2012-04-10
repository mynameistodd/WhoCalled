package com.mynameistodd.whocalled;

import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MissedCallsList extends ListActivity {

	private Context curContext;
	public String number;
	String result = "";
	String baseURL = "http://whocalled.us/do?action=getWho&name=test&pass=test&phoneNumber=";
	GoogleAnalyticsTracker tracker;
	ProgressDialog progressDialog;
	String FILENAME = "names_numbers";
	public static String PREFERENCES_NAME = "WhoCalled";
	private Util whoCalledUtil;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		
		curContext = this;
		whoCalledUtil = new Util(curContext);
		
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.startNewSession("UA-26489424-1", this);
		
		Intent intent = getIntent();
		int callType = intent.getIntExtra("com.mynameistodd.whocalled.calllist", 0);
		
		String selection = CallLog.Calls.TYPE + '=';
		switch (callType) {
		default:
		case 1:
			selection += CallLog.Calls.MISSED_TYPE;
			tracker.trackPageView("/callList/view/missed");
			break;
		case 2:
			selection += CallLog.Calls.OUTGOING_TYPE;
			tracker.trackPageView("/callList/view/outgoing");
			break;
		case 3:
			selection += CallLog.Calls.INCOMING_TYPE;
			tracker.trackPageView("/callList/view/incoming");
			break;
		}
		
		selection += " and " + CallLog.Calls.CACHED_NAME + " IS NULL";
		
		Cursor c = managedQuery(CallLog.Calls.CONTENT_URI, null,selection,null,CallLog.Calls.DEFAULT_SORT_ORDER);
		
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this, R.layout.list_view_items, c, new String[] { CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.NUMBER }, new int[] { R.id.displayNumber, R.id.displayDate, R.id.displayName })
		{
			@Override
			public void setViewText(TextView v, String text) {
				super.setViewText(v, text);
				if (v.getId() == R.id.displayNumber)
				{
					v.setText(PhoneNumberUtils.formatNumber(text));
				}
				else if (v.getId() == R.id.displayName)
				{
					String fromCache = whoCalledUtil.readFromCache(text);
					v.setText(fromCache);
				}
				else
				{
					v.setText(DateUtils.formatDateTime(curContext, Long.parseLong(text), DateUtils.FORMAT_NUMERIC_DATE));
				}
			}
		};

		setListAdapter(sca);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		RelativeLayout relativeMaster = (RelativeLayout)v;
		TextView tv = (TextView)relativeMaster.getChildAt(2);
		
		number = (String) tv.getText();
		number = number.replace("-", "");
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(true);
		progressDialog.setMessage("Loading...");
		progressDialog.show();
        
        registerForContextMenu(getListView());
        Thread myThread = new Thread(new Runnable() {
            public void run() {
            	Looper.prepare();
				getWhoCalledResponse();
				processResultAndShowDialog();
                progressDialog.dismiss();
                Looper.loop();	
            }
        });
        myThread.start();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.item_long_click, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  //AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  switch (item.getItemId()) {
	  case R.id.item1:
		  Uri telNum = Uri.parse("tel:" + number);
		  startActivity(new Intent(Intent.ACTION_DIAL, telNum));
	    return true;
	  default:
	    return super.onContextItemSelected(item);
	  }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == RESULT_OK)
		{
			Toast.makeText(curContext, "Submitted!", Toast.LENGTH_SHORT).show();
			tracker.trackPageView("/callList/submited/"+number);
		}
		else
		{
			String response = "Error: ";
			if (data != null)
			{
				response += data.getStringExtra("com.mynameistodd.whocalled.response");
			}
			Toast.makeText(curContext, response, Toast.LENGTH_LONG).show();
			tracker.trackPageView("/callList/error/"+number);
		}
	}

	public void getWhoCalledResponse(){  
        HttpClient httpclient = new DefaultHttpClient();  
        HttpGet request = new HttpGet(baseURL + number);
        Log.d("mynameistodd", "request: " + baseURL + number);
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
	
	public void processResultAndShowDialog()
	{
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
	        	whoCalledUtil.saveToCache(number, who);
	        }
        }
        
        tracker.trackPageView("/callList/click/"+number);

		AlertDialog.Builder dialog = new AlertDialog.Builder(curContext);
		dialog.setTitle("Most Popular Response");
		dialog.setMessage(who);
		dialog.setPositiveButton(R.string.submit_your_response,
				new DialogInterface.OnClickListener() {
			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(curContext, SubmitResponse.class);
						intent.putExtra("com.mynameistodd.whocalled.unknownNumber", number);
						startActivityForResult(intent, 1);
					}
				});
		
		dialog.show();
	}

	@Override
	protected void onStop() {
		super.onStop();
		boolean result = tracker.dispatch();
		Log.d("mynameistodd", "analytics submitted: " + result);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.stopSession();
	}
}
