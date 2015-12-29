package com.coms309r04.drawnear.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.tools.IntentSwitcher;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendsActivity extends Activity {
	ArrayList<String> usernameList = new ArrayList<String>();
	ListView friendsList;
	ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends);

		friendsList = (ListView) findViewById(R.id.friendsList);

		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usernameList);
		friendsList.setAdapter(adapter);

		TextView loggedInAs = (TextView) findViewById(R.id.logged_in_as_friends);
		loggedInAs.setText(ParseUser.getCurrentUser().getUsername());
//
//		//Set up ActionBar Tabs
//		ActionBar actionBar = getActionBar();
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//
//		Tab tabNearby = actionBar.newTab().setText("Nearby").setTabListener(new TabListener<NearbyActivity>(this, NearbyActivity.class));
//		actionBar.addTab(tabNearby);
//		Tab tabMap = actionBar.newTab().setText("Map").setTabListener(new TabListener<MapActivity>(this, MapActivity.class));
//		actionBar.addTab(tabMap);
//		Tab tabFriends = actionBar.newTab().setText("Friends").setTabListener(new TabListener<FriendsActivity>(this, FriendsActivity.class));
//		actionBar.addTab(tabFriends);
//
//		actionBar.selectTab(tabFriends);
//		TabListener.tabsActive = true;

		getFriends();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.navigation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Temporary demo functionality to access all views with the settings menu
		switch(item.getItemId()) {
			case(android.R.id.home):
				NavUtils.navigateUpFromSameTask(this);
				return true;
			default:
				Intent intent = IntentSwitcher.onOptionsNavigationSelected(item.getItemId(), this);
				if(intent != null) {
					startActivity(intent);
				}
		}
		return super.onOptionsItemSelected(item);
	}

	public void addFriend(View v) {
		String username = ((EditText) findViewById(R.id.add_friend_username)).getText().toString();

		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", username);
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> users, ParseException e) {
				if(e == null) {
					if(users.size() == 0) {
						Toast.makeText(FriendsActivity.this,
								"This user does not exist.", Toast.LENGTH_SHORT)
								.show();
					} else {
						final ParseUser userToAdd = users.get(0);
						// List<ParseUser> a = new ArrayList<ParseUser>();
						// a.add(userToAdd);

						if(usernameList.contains(userToAdd.getUsername())) {
							Toast.makeText(
									FriendsActivity.this,
									"You are already friends with "
											+ userToAdd.getUsername(),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(
									FriendsActivity.this,
									"Added friends with "
											+ userToAdd.getUsername(),
									Toast.LENGTH_SHORT).show();

							ParseUser.getCurrentUser().add("friendsList", userToAdd);

							ParseUser.getCurrentUser().saveInBackground(
								new SaveCallback() {
									public void done(ParseException e) {
										if(e == null) {
											getFriends();
										} else {
											Toast.makeText(
												FriendsActivity.this,
												"Friend did not save correctly",
												Toast.LENGTH_SHORT)
												.show();
										}
									}
								}
							);
						}
					}
				} else {
					Toast.makeText(FriendsActivity.this, "Error adding friend.", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void getFriends() {
		List<ParseUser> friends = (List<ParseUser>) ParseUser.getCurrentUser().get("friendsList");

		if(friends != null) {
			usernameList.clear();
			for(final ParseUser friend : friends) {
				friend.fetchIfNeededInBackground(new GetCallback<ParseUser>() {
					@Override
					public void done(ParseUser object,
							com.parse.ParseException e) {
						usernameList.add((String) friend.get("username"));
						Collections.sort(usernameList);
						adapter.notifyDataSetChanged();
					}
				});
			}
		}
	}
}
