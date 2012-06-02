package com.mynameistodd.whocalled;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class About extends Activity {

	TextView aboutTextView;
	Button btnAboutOK;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		aboutTextView = (TextView)findViewById(R.id.aboutTextView);
		btnAboutOK = (Button)findViewById(R.id.btn_about_ok);
		
		aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
		aboutTextView.setText(Html.fromHtml("Developed by <a href=\"http://www.todddeland.com\">Todd DeLand</a><br /><br />Check it out on <a href=\"http://www.facebook.com/WhoCalledUs\">Facebook</a>"));
		btnAboutOK.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}