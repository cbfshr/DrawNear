package com.coms309r04.drawnear.views;
import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.R.id;
//import com.coms309r04.drawnear.data.UserItem;

import com.coms309r04.drawnear.tools.MyUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class ProfileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		//UserItem userA = new UserItem("John","Doe","johndoe","i am having a good day");
		TextView text = (TextView) findViewById(R.id.textView1);
		//text.setText(userA.getName());
		EditText text1 = (EditText) findViewById(id.editText1);
		//text1.setText(userA.getstatus());
		
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
		startActivity(MyUtils.onOptionsNavigationSelected(item.getItemId(), this));
		
		return super.onOptionsItemSelected(item);
	}
	
	
}
