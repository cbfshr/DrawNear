package com.coms309r04.drawnear.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.Manifest;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coms309r04.drawnear.R;
import com.coms309r04.drawnear.connection.DrawingManager;
import com.coms309r04.drawnear.connection.GPSManager;
import com.coms309r04.drawnear.connection.ILocationUpdater;
import com.coms309r04.drawnear.data.DrawingItem;
import com.coms309r04.drawnear.tools.IntentSwitcher;
import com.coms309r04.drawnear.tools.MapManager;
import com.coms309r04.drawnear.tools.TabListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

// In order for GoogleMaps to work on your machine...
// 1. Add debug key so API key works on your machine
// 2. Add google-play-services_lib as a library project to your application
// ( I copied it into my workspace (it is not in the SVN repo) )

public class MapActivity
	extends FragmentActivity
	implements ILocationUpdater, OnMarkerClickListener, OnMapClickListener, OnInfoWindowClickListener {

	private static final int GPS_ERRORDIALOG_REQUEST = 9001;
	private static final int MENU_GET_CURRENT_LOCATION = 9002;
	private static final int ALLOW_LOCATION_SERVICES = 9999;

	@SuppressWarnings("unused")
	private static final double ISU_LAT = 42.025410, ISU_LNG = -93.646085;
	private static final float DEFAULTZOOM = 14.5f;

	boolean infoWindowIsShowing = false;

	// HashMap<String, DrawingItem> nearbyDrawings;
	// markerId, drawingId
	HashMap<String, String> markerIDsToDrawingsIDs = new HashMap<String, String>();

	// markerId, Marker
	HashMap<String, Marker> markerIDsToMarkers = new HashMap<String, Marker>();

	// list of markers in order by distance so the user can scroll through info windows
	ArrayList<Marker> markerReferences = new ArrayList<Marker>();
	Marker selectedMarker = null;

	ProgressBar pbMap;
	ImageButton arrowLeft;
	ImageButton arrowRight;
	Animation animationFadeIn;
	Animation animationFadeOut;

	GoogleMap mMap;
	Circle radius;
	boolean locationUpdatesAvailable = false;
	// LocationClient mLocationClient;

	private GPSManager gps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Parse.initialize(this, "IxoHKtA2ofmKj4KtIaB9HTwQCiM9LZCV7ZgUx7t9",
		// "QcvV1SxpnNvvgv2Ti9aG0Es83iwOxRwVqCdoNQ4W");

		//Set up ActionBar Tabs
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Tab tabNearby = actionBar.newTab().setText("Nearby").setTabListener(new TabListener<NearbyActivity>(this, NearbyActivity.class));
		actionBar.addTab(tabNearby);	
		Tab tabMap = actionBar.newTab().setText("Map").setTabListener(new TabListener<MapActivity>(this, MapActivity.class));
		actionBar.addTab(tabMap);
		actionBar.selectTab(tabMap);

		TabListener.tabsActive = true;

		if (servicesOK()) {
			setContentView(R.layout.activity_map);

			pbMap = (ProgressBar) findViewById(R.id.progressBar2);
			pbMap.setVisibility(View.INVISIBLE);

			arrowLeft = (ImageButton) findViewById(R.id.map_arrow_left);
			arrowLeft.setVisibility(View.INVISIBLE);

			arrowRight = (ImageButton) findViewById(R.id.map_arrow_right);
			arrowRight.setVisibility(View.INVISIBLE);

			animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
			animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);

			TextView loggedInAs = (TextView) findViewById(R.id.logged_in_as_map);
			loggedInAs.setText(ParseUser.getCurrentUser().getUsername());

			if(initMap()) {
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(
						this,
						new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
						ALLOW_LOCATION_SERVICES
					);
				} else {
					resetMap();
				}

				// mLocationClient = new LocationClient(this, this, this);
				// mLocationClient.connect();
			} else {
				Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT).show();
			}
		} else {
			setContentView(R.layout.activity_map_no_map);
		}
	}

	private void resetMap() {
		gps = new GPSManager(this);

		try {
			mMap.setMyLocationEnabled(true);
		} catch(SecurityException e) {
			Log.e("Map", "Unable to set location enabled");
		}

		// Get Last Location
		ParseGeoPoint lastLocation = null;
		if(gps != null) {
			lastLocation = gps.getLastLocation();
		}

		//Draw the circle around last location
		if(lastLocation != null) {
			drawRadius(lastLocation.getLatitude(), lastLocation.getLongitude(), (int)(DrawingManager.radius * 1000));
		}

		// Zoom into location on the map
		gotoCurrentLocationDefaultZoom();

		// Reload the drawings
		loadAndUpdateDrawings();

		// Show the markers for all drawings.
		drawMarkers();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case ALLOW_LOCATION_SERVICES: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay! Do the task you need to do.
					try {
						resetMap();
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.navigation, menu);
		// Add "Get Current Location" button to action bar
		MenuItem currentLocation = menu.add(0, MENU_GET_CURRENT_LOCATION,
				Menu.NONE, R.string.get_current_location).setIcon(
				R.drawable.ic_refresh);
		currentLocation.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Temporary demo functionality to access all views with the settings menu

		Intent intent = IntentSwitcher.onOptionsNavigationSelected(item.getItemId(), this);
		if(intent != null) {
			// views that should load "on top" of view
			if(item.getItemId() != R.id.action_goto_create_post) {
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			startActivity(intent);
		} else if(item.getItemId() == MENU_GET_CURRENT_LOCATION) {
			gotoCurrentLocationDefaultZoom();
			loadAndUpdateDrawings();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		if(gps != null) {
			gps.stopGPS();
		}
		super.onStop();
		MapManager mgr = new MapManager(this);
		mgr.saveMapState(mMap);
	}

	@Override
	protected void onResume() {
		if (gps != null && !gps.isRunning()) {
			gps.resumeGPS();
		}

		super.onResume();

		MapManager mgr = new MapManager(this);
		CameraPosition position = mgr.getSavedCameraPosition();
		if (position != null) {
			CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
			mMap.moveCamera(update);
		}
	}

	private void loadAndUpdateDrawings() {
		// Get nearby drawings
		if (gps != null && gps.getLastLocation() == null) {
			Toast.makeText(this, "Can't get last location", Toast.LENGTH_SHORT).show();
		}

		/*Toast.makeText(this, "Checking for new drawings...", Toast.LENGTH_SHORT).show();*/
		MyMapTask getDrawingsThread = new MyMapTask();
		getDrawingsThread.execute();
	}

	public boolean servicesOK() {
		int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (isAvailable == ConnectionResult.SUCCESS) {
			return true;
		} else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
			dialog.show();
		} else {
			Toast.makeText(this, "Can't connect to Google Play services", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	private boolean initMap() {
		if (mMap == null) {
			SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
			mMap = mapFrag.getMap();

			if (mMap != null) {
				mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
					@Override
					public View getInfoWindow(Marker arg0) {
						return null;
					}

					@Override
					public View getInfoContents(Marker marker) {
						View v = getLayoutInflater().inflate(R.layout.map_info_window, null);
						TextView tvTitle = (TextView) v.findViewById(R.id.map_drawing_title);
						TextView tvDistance = (TextView) v.findViewById(R.id.map_drawing_distance);
						TextView tvCreator = (TextView) v.findViewById(R.id.map_drawing_creator);

						// Image
						ImageView ivThumb = (ImageView) v.findViewById(R.id.map_thumbnail);

						// Title
						tvTitle.setText(marker.getTitle());

						DrawingItem d = DrawingManager
								.getInstance()
								.getCurrentNearbyDrawings()
								.get(markerIDsToDrawingsIDs.get(marker.getId()));

						// Distance
						float miles = (float) d.getDistanceInMiles();
						if (miles >= 0.05) {
							tvDistance.setText(String.format("%.2f", miles) + "mi");
						} else {
							tvDistance.setText((int) (miles * 5280) + "ft");
						}

						// Creator
						if (d.getCreator() != null) {
							ParseUser u = d.getCreator();
							if (u.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
								tvCreator.setText("You");
							} else {
								tvCreator.setText(u.getString("username"));
							}
						}

						if(d.getThumbnail() != null) {
							ivThumb.setImageBitmap(d.getThumbnail());
						}
						return v;
					}
				});

				mMap.setOnMarkerClickListener((OnMarkerClickListener) this);
				mMap.setOnMapClickListener((OnMapClickListener) this);
				mMap.setOnInfoWindowClickListener((OnInfoWindowClickListener) this);
			}
		}
		return (mMap != null);
	}

	protected void gotoCurrentLocationDefaultZoom() {
		ParseGeoPoint currentLocation = null;
		if(gps != null) {
			currentLocation = gps.getLastLocation();
		}
		if(currentLocation == null) {
			Toast.makeText(this, "Current location isn't available", Toast.LENGTH_SHORT).show();
		} else {
			LatLng ll = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, DEFAULTZOOM);
			mMap.animateCamera(update);
		}
	}

	protected void gotoLocation(LatLng location) {
		CameraUpdate update = CameraUpdateFactory.newLatLng(location);
		mMap.animateCamera(update);
	}

	private void loadImageAndUpdateMarker(final String id, ParseFile bitmap) {
		bitmap.getDataInBackground(new GetDataCallback() {
			// TO DO: UPDATE MARKERS AND ADD IMAGE TO IT
			@Override
			public void done(byte[] data, ParseException e) {
				if(e == null) {
					// First decode with inJustDecodeBounds=true to check
					// dimensions
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeByteArray(data, 0, data.length, options);

					// Calculate inSampleSize
					options.inSampleSize = DrawingManager.calculateInSampleSize(options, 200, 200);

					// Decode bitmap with inSampleSize set
					options.inJustDecodeBounds = false;
					Bitmap thumbBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

					// Set this as the thumbnail for the appropriate drawing
					DrawingManager.getInstance().getCurrentNearbyDrawings().get(id).setThumbnail(thumbBitmap);
				} else {
					e.printStackTrace();
				}
			}
		});
	}

	private void drawRadius(double lat, double lng, int r) {
		if(radius != null) {
			radius.remove();
			radius = null;
		}

		LatLng ll = new LatLng(lat, lng);

		CircleOptions options = new CircleOptions().center(ll).radius(r).fillColor(0x11FF00FF).strokeColor(Color.BLUE).strokeWidth(3);
		radius = mMap.addCircle(options);
	}

	private void drawMarkers() {
		for(DrawingItem drawing : DrawingManager.getInstance().getCurrentNearbyDrawings().values()) {
			// only create a marker for a drawing if it has not already been created
			if(!(markerIDsToDrawingsIDs.values().contains(drawing.getId()))) {
				MarkerOptions options = new MarkerOptions()
						.title(drawing.getTitle())
						.position(new LatLng(
							drawing.getLocation().getLatitude(),
							drawing.getLocation().getLongitude())
						)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pencil_marker));

				Marker m = mMap.addMarker(options);
				drawing.setMarkerId(m.getId());

				// Add the marker to marker lists/association maps
				this.markerIDsToDrawingsIDs.put(m.getId(), drawing.getId());
				this.markerIDsToMarkers.put(m.getId(), m);
				this.markerReferences.add(m);
			}
		}

		// Delete markers that are no longer nearby (needs to use iterator for
		// deletion)
		Iterator<Map.Entry<String, String>> iter = this.markerIDsToDrawingsIDs.entrySet().iterator();

		while (iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();

			// If the drawing associated with a marker is no longer in the
			// nearby drawings list in DrawingManager...
			if(!(DrawingManager.getInstance().getCurrentNearbyDrawings()
					.keySet().contains(entry.getValue()))) {
				// Get reference to the marker
				Marker toRemove = this.markerIDsToMarkers.get(entry.getKey());

				// remove marker from the map
				toRemove.remove();

				// remove from markerReferences list
				this.markerReferences.remove(toRemove);

				// remove from marker association hashmap
				this.markerIDsToMarkers.remove(entry.getKey());

				// Remove from drawing association hashmap
				iter.remove();
			}
		}

		/*Toast.makeText(this, "Drawing Markers: " + this.markerReferences.size(), Toast.LENGTH_SHORT).show();*/

		sortMarkerReferencesByDistance();
	}

	// TO DO:
	// If this affects how drawings show up on the map, this function
	// will need to sort DrawingManager.currentDrawings
	//
	// Sort the arraylist of marker references by their distance (getting
	// distances from nearbyDrawings hashmap)
	private void sortMarkerReferencesByDistance() {
		Collections.sort(markerReferences, new Comparator<Marker>() {
			@Override
			public int compare(Marker m1, Marker m2) {
				return Double.compare(
					DrawingManager.getInstance().getCurrentNearbyDrawings()
						.get(markerIDsToDrawingsIDs.get(m1.getId()))
						.getDistanceInMiles(),
					DrawingManager.getInstance().getCurrentNearbyDrawings()
						.get(markerIDsToDrawingsIDs.get(m2.getId()))
						.getDistanceInMiles()
				);
			}
		});
	}

	@Override
	public void locationChanged(double lat, double lng, float distanceFromLastLocation, boolean updatedLastLocation) {
		/*Toast.makeText(this, "Lat: " + lat + " Long: " + lng + " " + distanceFromLastLocation, Toast.LENGTH_LONG).show();*/

		drawRadius(lat, lng, (int) (DrawingManager.radius * 1000));

		if(updatedLastLocation) {
			loadAndUpdateDrawings();
			/*Toast.makeText(this, "Updating drawings.", Toast.LENGTH_SHORT).show();*/
		} else {
			/*Toast.makeText(this, "Not far enough from last location to update drawings.", Toast.LENGTH_SHORT).show();*/
		}
	}

	@Override
	public void locationUpdatesAvailable() {
		Toast.makeText(this, "Connected to location services", Toast.LENGTH_LONG).show();
		ParseGeoPoint lastLocation = null;

		if(gps != null) {
			lastLocation = gps.getLastLocation();
		}
		if(lastLocation != null) {
			drawRadius(lastLocation.getLatitude(), lastLocation.getLongitude(), (int)(DrawingManager.radius * 1000));
		}

		loadAndUpdateDrawings();

		gotoCurrentLocationDefaultZoom();
		//resetMap();
	}

	private class MyMapTask extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			// has access to main thread
			Log.i("MAIN", "Starting task");

			// DrawingManager.currentDrawings
			if(DrawingManager.getInstance().getCurrentNearbyDrawings() == null) {
				if(pbMap != null) {
					pbMap.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		protected String doInBackground(String... params) {
			ParseGeoPoint last = null;
			if(gps != null) {
				last = gps.getLastLocation();
			}

			if(last != null) {
				DrawingManager.getInstance().getNearbyDrawings(last, 100);
				// Delete these drawings :)
				DrawingManager.getInstance().removeDrawingsNoLongerNearby(last);

				return "Success";
			}
			return "Failure";
		}

		@Override
		protected void onPostExecute(String result) {
			// has access to main thread
			// update display here

			if(result == null) {
				Toast.makeText(MapActivity.this, "Could not connect to receive drawings", Toast.LENGTH_SHORT).show();
			}

			// Get rid of progress bar
			pbMap.setVisibility(View.INVISIBLE);

			// drawMarkers
			drawMarkers();

			// load in each picture one by one (if they have not previously been loaded)
			for(DrawingItem d : DrawingManager.getInstance().getCurrentNearbyDrawings().values()) {
				if(d.getBitmapFile() != null && d.getThumbnail() == null) {
					loadImageAndUpdateMarker(d.getId(), d.getBitmapFile());
				}
			}

			pbMap.setVisibility(View.INVISIBLE);
		}
	}

	// The following methods are used to for arrow functionality
	@Override
	public void onMapClick(LatLng arg0) {
		infoWindowIsShowing = false;
		selectedMarker = null;
		enableArrows(false);
	}

	@Override
	public boolean onMarkerClick(Marker m) {
		infoWindowIsShowing = true;
		selectedMarker = m;
		enableArrows(true);
		return false;
	}

	private void enableArrows(boolean b) {
		if(b) {
			arrowLeft.setVisibility(View.VISIBLE);
			arrowRight.setVisibility(View.VISIBLE);
			arrowLeft.startAnimation(animationFadeIn);
			arrowRight.startAnimation(animationFadeIn);
		} else {
			arrowLeft.startAnimation(animationFadeOut);
			arrowRight.startAnimation(animationFadeOut);
			arrowLeft.setVisibility(View.INVISIBLE);
			arrowRight.setVisibility(View.INVISIBLE);
		}
	}

	public void onArrowClick(View v) {
		if(infoWindowIsShowing) {
			int nextIndex;
			switch (v.getId()) {
				case R.id.map_arrow_left:
					nextIndex = (markerReferences.indexOf(selectedMarker) - 1) % markerReferences.size();
					if(nextIndex < 0) {
						nextIndex += markerReferences.size();
					}
					break;
				case R.id.map_arrow_right:
					nextIndex = Math.abs((markerReferences.indexOf(selectedMarker) + 1) % markerReferences.size());
					break;
				default:
					return;
			}

			// Toast.makeText(this, "Index is " + nextIndex, Toast.LENGTH_SHORT).show();
			selectedMarker = markerReferences.get(nextIndex);
			selectedMarker.showInfoWindow();
			gotoLocation(selectedMarker.getPosition());
			return;
		}
	}

	@Override
	public void onInfoWindowClick(Marker m) {
		Intent intent = new Intent(MapActivity.this, DisplayPostActivity.class);
		intent.putExtra("id", markerIDsToDrawingsIDs.get(m.getId()));
		startActivity(intent);
	}
}
