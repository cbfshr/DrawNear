package com.coms309r04.drawnear.tools;

import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.views.CreatePostActivity;
import com.coms309r04.drawnear.views.DispatchActivity;
import com.coms309r04.drawnear.views.FriendsActivity;
import com.coms309r04.drawnear.views.MapActivity;
import com.coms309r04.drawnear.views.NearbyActivity;
import com.coms309r04.drawnear.views.ProfileActivity;
import com.parse.ParseUser;
import android.content.Context;
import android.content.Intent;

public class MyUtils {
	//Return an Intent to each view (activity) based on selecting that view from the options menu
	//This is a helper method to easily navigate between pages for initial demoing purposes.
	//It will not be in the final application
	public static Intent onOptionsNavigationSelected(int id, Context fromActivity) {
		Intent intent = null;

		switch (id) {
			/*case R.id.action_goto_nearby:
				intent = new Intent(fromActivity, NearbyActivity.class);
				break;
			case R.id.action_goto_map:
				intent = new Intent(fromActivity, MapActivity.class);
				break;*/
			case R.id.action_goto_create_post:
				intent = new Intent(fromActivity, CreatePostActivity.class);
				break;
//			case R.id.action_goto_profile:
//				intent = new Intent(fromActivity, ProfileActivity.class);
//				break;
			/*case R.id.action_goto_settings:
				intent = new Intent(fromActivity, SettingsActivity.class);
				break;*/
//			case R.id.action_goto_friends:
//				intent = new Intent(fromActivity, FriendsActivity.class);
//				break;
			case R.id.action_logout:
				ParseUser.logOut();
				// Start and intent for the dispatch activity
				intent = new Intent(fromActivity, DispatchActivity.class);
				break;
			default:
				break;
		}

		return intent;
	}
}
