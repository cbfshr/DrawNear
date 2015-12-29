package com.coms309r04.drawnear.views;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.tools.IntentSwitcher;

public class MainActivity extends FragmentActivity {
	/**
	 * The number of pages (wizard steps) to show in this demo.
	 */
	private static final int NUM_PAGES = 2;
	private static final int ALLOW_LOCATION_SERVICES = 2;

	/**
	 * The pager widget, which handles animation and allows swiping horizontally to access previous
	 * and next wizard steps.
	 */
	private ViewPager mPager;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;

	public static android.support.v4.app.FragmentManager fragmentManager;

	NearbyFragment nearbyFragment;
	MapViewFragment mapViewFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen_slide);

		nearbyFragment = NearbyFragment.newInstance();
		mapViewFragment = MapViewFragment.newInstance();

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
		mPager.setAdapter(mPagerAdapter);

		// Check if Location is allowed on the device
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(
				this,
				new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
				ALLOW_LOCATION_SERVICES
			);
		}

		fragmentManager = getSupportFragmentManager();
	}

	/**
	 * This function is a callback function required to handle the result
	 * from a request permission dialog (in this case, for the user
	 * allowing the application to use LocationServices.
	 *
	 * @param requestCode
	 * @param permissions
	 * @param grantResults
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case ALLOW_LOCATION_SERVICES: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay! Do the task you need to do.
					try {
						//loadAndUpdateDrawings();
					} catch(SecurityException e) {
						Log.e("Map", "Not able to set location.");
					}
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Log.e("Map", "Permissions not granted to use location.");

					// Exit the application
					/*Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_HOME);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);*/
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.navigation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = IntentSwitcher.onOptionsNavigationSelected(item.getItemId(), this);

		switch (item.getItemId()) {
			case android.R.id.home:
				return true;
			default:
				if(intent != null) {
					if(item.getItemId() != R.id.action_goto_create_post) {
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
					}
					startActivity(intent);
				}
		}

		return super.onOptionsItemSelected(item);
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch(position) {
				case(0):
					return nearbyFragment;
				case(1):
					return mapViewFragment;
			}
			return nearbyFragment;
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}
}
