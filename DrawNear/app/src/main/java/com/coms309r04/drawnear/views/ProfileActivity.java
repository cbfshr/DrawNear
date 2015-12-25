package com.coms309r04.drawnear.views;

import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.R.id;

import com.coms309r04.drawnear.tools.MyUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ProfileActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		//getActionBar().setDisplayHomeAsUpEnabled(true);

		Button gotoFriends = (Button) findViewById(id.goto_friends_activity);
		gotoFriends.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent friendsIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
				startActivity(friendsIntent);
			}
		});

		//UserItem userA = new UserItem("John","Doe","johndoe","i am having a good day");
		//TextView text = (TextView) findViewById(R.id.textView1);
		//text.setText(userA.getName());
		//EditText text1 = (EditText) findViewById(id.editText1);
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
		switch(item.getItemId()) {
			case(android.R.id.home):
				//NavUtils.navigateUpFromSameTask(this);
				final Intent intent1 = NavUtils.getParentActivityIntent(this);
				NavUtils.navigateUpTo(this, intent1);
				return true;
			default:
				Intent intent = MyUtils.onOptionsNavigationSelected(item.getItemId(), this);
				if(intent != null) {
					startActivity(intent);
				}
		}
		return super.onOptionsItemSelected(item);
	}
}
