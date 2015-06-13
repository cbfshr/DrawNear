package com.coms309r04.drawnear.views;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseACL;
import com.parse.ParseRole;
import com.parse.ParseUser;

/**
 * Activity which starts an intent for either the logged in (NearbyActivity) or logged out
 * (SignUpOrLoginActivity) activity.
 */
public class DispatchActivity extends Activity {

  public DispatchActivity() {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Check if there is current user info
    if (ParseUser.getCurrentUser() != null) {
      // Start an intent for the logged in activity
 	
    	startActivity(new Intent(this, NearbyActivity.class));
    } else {
      // Start an intent for the logged out activity
     startActivity(new Intent(this, WelcomeActivity.class));
    }
  }

}
