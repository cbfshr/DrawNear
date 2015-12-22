package com.coms309r04.drawnear.views;



import android.app.Application;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class DrawNearApplication extends Application {

	
	@Override
	  public void onCreate() {
	    super.onCreate();

	    // Add your initialization code here
	    //Parse.enableLocalDatastore(getApplicationContext());
	    Parse.initialize(this, "IxoHKtA2ofmKj4KtIaB9HTwQCiM9LZCV7ZgUx7t9", "QcvV1SxpnNvvgv2Ti9aG0Es83iwOxRwVqCdoNQ4W");
	    //ParseUser.enableAutomaticUser();
		//ParseACL defaultACL = new ParseACL();
		//ParseACL.setDefaultACL(defaultACL, true);
	}
}