package com.mynameistodd.whocalled;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.util.Log;


public class whocalledReceiver extends BroadcastReceiver {
	Context curContext;
	PhoneStateListener listener;

	@Override
	public void onReceive(Context context, Intent intent) {
		TelephonyManager telMan = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		curContext = context;
		
        listener = new PhoneStateListener() {
        	public void onCallStateChanged(int state, String incomingNumber) {
        		Log.d("mynameistodd", "BR unknownNumber: " + incomingNumber);
        		try
        		{
	        		if (incomingNumber != null && incomingNumber.length() > 0 && Long.parseLong(incomingNumber) > 0)
	        		{
		        		switch (state)
		        		{
		        		case TelephonyManager.CALL_STATE_IDLE:
		        			
		        			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
			        		Cursor c = curContext.getContentResolver().query(uri, new String[]{PhoneLookup._ID},null,null,null);
			        		Log.d("mynameistodd", "cursorCount: " + c.getCount());
			        		
			        		if (c.getCount() <= 0)
			        		{
			        			int icon = R.drawable.icon;
			        			long when = System.currentTimeMillis();
			        			String displayName = readFromCache(incomingNumber);
			        			
			        			CharSequence tickerText = displayName + " - " + incomingNumber;
			        			CharSequence contentTitle = displayName + " - " + incomingNumber;
			        			CharSequence contentText = "Tap to search WhoCalled.us";
			        			
			        			NotificationManager mNotificationManager = (NotificationManager) curContext.getSystemService(Context.NOTIFICATION_SERVICE);
			        			Notification notification = new Notification(icon, tickerText, when);
			        			notification.flags = Notification.FLAG_AUTO_CANCEL;
			        			
			        			Intent notificationIntent = new Intent(curContext, WhoCalled.class);
			        			notificationIntent.putExtra("com.mynameistodd.whocalled.unknownNumber", incomingNumber);
			        			PendingIntent contentIntent = PendingIntent.getActivity(curContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
			        			notification.setLatestEventInfo(curContext, contentTitle, contentText, contentIntent);
			        			mNotificationManager.notify(1, notification);
			        		}
		        			break;
		        		}
	        		}
        		}
        		catch (NumberFormatException ex)
        		{
        			Log.d("mynameistodd", "BR unknownNumber: " + incomingNumber + "exception:" + ex.toString());
        		}
        	};
        };
	        
        telMan.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	
	public String readFromCache(String number)
	{
		SharedPreferences settings = curContext.getSharedPreferences(MissedCallsList.PREFERENCES_NAME, Context.MODE_PRIVATE);
	    return settings.getString(number, "Unknown Caller");
	}
}
