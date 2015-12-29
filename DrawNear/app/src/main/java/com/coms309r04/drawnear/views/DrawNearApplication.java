package com.coms309r04.drawnear.views;

import android.app.Application;
import android.os.SystemClock;
import com.parse.Parse;

public class DrawNearApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		// This is bad practice and should not be implemented in the final product
		// Hopefully, as the app becomes larger, the start-up will take longer (you know what I mean)
		// Ideally, we should initialize Google Play Services, Locations, etc. here - I don't know how
		// to do that right now.
		SystemClock.sleep(4000);

		// Add your initialization code here
		//Parse.enableLocalDatastore(getApplicationContext());
		Parse.initialize(this, "IxoHKtA2ofmKj4KtIaB9HTwQCiM9LZCV7ZgUx7t9", "QcvV1SxpnNvvgv2Ti9aG0Es83iwOxRwVqCdoNQ4W");
		//ParseUser.enableAutomaticUser();
		//ParseACL defaultACL = new ParseACL();
		//ParseACL.setDefaultACL(defaultACL, true);
	}
}
