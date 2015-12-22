package com.coms309r04.drawnear.views;
import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.tools.MyUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View;
public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings1);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	

	Button profile = (Button) findViewById(R.id.Profile);
	Button account = (Button) findViewById(R.id.Account_setting);
	Button loc = (Button) findViewById(R.id.Location_setting);
	Button pvt = (Button) findViewById(R.id.Privacy_setting);
	Button adv = (Button) findViewById(R.id.Advanced_setting);
	Button about = (Button) findViewById(R.id.About_app);
	Button help = (Button) findViewById(R.id.Help_app);
	Button toggle = (Button) findViewById(R.id.toggleButton1);
	
	
	
	account.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(SettingsActivity.this, AccountActivity.class);
			startActivity(intent);
			// TODO Auto-generated method stub
			
		}
	});
	
	
	 
	
		
	
	}
	

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.navigation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Temporary demo functionality to access all views with the settings menu
		
		
		//startActivity(MyUtils.onOptionsNavigationSelected(item.getItemId(), this));
		if (item.getItemId() == android.R.id.home) {
			finish();
			
			}else
				startActivity(MyUtils.onOptionsNavigationSelected(item.getItemId(), this));
				finish();
		return super.onOptionsItemSelected(item);
		
	}
	
	
}
