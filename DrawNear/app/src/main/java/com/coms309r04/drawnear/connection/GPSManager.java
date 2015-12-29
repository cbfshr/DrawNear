package com.coms309r04.drawnear.connection;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.parse.ParseGeoPoint;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class GPSManager {
	private static double ISU_LAT = 42.025410, ISU_LNG = -93.646085;

	private Context main;
	private ILocationUpdater fragment = null;

	// Helper for GPS-Position
	private MyLocationListener mlocListener;
	private LocationRequest request;

	private boolean isRunning;

	private Location lastUpdateLocation = null;

	public GPSManager(Context activity) {
		this.main = activity;

		mlocListener = new MyLocationListener(activity);
		request = LocationRequest.create();
		request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		request.setInterval(20000);
		request.setFastestInterval(5000);

		this.isRunning = true;
	}

	public GPSManager(Context activity, ILocationUpdater fragment) {
		this(activity);
		this.fragment = fragment;
	}

	public void stopGPS() {
		if(isRunning) {
			mlocListener.stopLocationUpdates();
			this.isRunning = false;
		}
	}

	public void resumeGPS() {
		mlocListener.startLocationUpdates();
		this.isRunning = true;
	}

	public boolean isRunning() {
		return this.isRunning;
	}

	public ParseGeoPoint getLastLocation(){
		Location last = mlocListener.getLastLocation();
		if (last == null){
			Log.e("GPS", "Cannot retrieve last known location");
			return null;
		}
		return new ParseGeoPoint(last.getLatitude(), last.getLongitude());
	}

	public Context getMain() {
		return this.main;
	}

	private class MyLocationListener
		implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

		private final String TAG = MyLocationListener.class.getSimpleName();

		GoogleApiClient mGoogleApiClient;

		public MyLocationListener(Context main) {
			if (mGoogleApiClient == null) {
				mGoogleApiClient = new GoogleApiClient.Builder(main)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
				mGoogleApiClient.connect();
			}
		}

		public void stopLocationUpdates() {
			if(mGoogleApiClient != null) {
				try {
					LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
				} catch (SecurityException e) {
					Log.e("GPS", "Remove Location Updates failed.");
				}
			}
		}

		public void startLocationUpdates() {
			if(mGoogleApiClient != null) {
				try {
					LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
				} catch (SecurityException e) {
					Log.e("GPS", "Request Location Updates failed.");
				}
			}
		}
		
		public Location getLastLocation(){
//			Location location = new Location("");
//			location.setLongitude(ISU_LNG);
//			location.setLatitude(ISU_LAT);
//			return location;

			try {
				return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
			} catch(SecurityException e) {
				Log.e("GPS", "Get Last Location failed.");
				return null;
			}
		}

		@Override
		public void onLocationChanged(Location loc) {
			if(lastUpdateLocation != null) {
				float distance = loc.distanceTo(lastUpdateLocation);

				// 20 meters
				if (distance > 20.0) {
					lastUpdateLocation = loc;
					if(fragment == null) {
						((ILocationUpdater)GPSManager.this.main).locationChanged(loc.getLatitude(), loc.getLongitude(), distance, true);
					} else {
						GPSManager.this.fragment.locationChanged(loc.getLatitude(), loc.getLongitude(), distance, true);
					}
				} else {
					if(fragment == null) {
						((ILocationUpdater)GPSManager.this.main).locationChanged(loc.getLatitude(), loc.getLongitude(), distance, false);
					} else {
						GPSManager.this.fragment.locationChanged(loc.getLatitude(), loc.getLongitude(), distance, false);
					}
				}
			} else {
				lastUpdateLocation = loc;
			}
		}

		@Override
		public void onConnectionFailed(ConnectionResult arg0) {

		}

		@Override
		public void onConnected(Bundle bundle) {
			startLocationUpdates();
			if(fragment == null) {
				((ILocationUpdater)GPSManager.this.main).locationUpdatesAvailable();
			} else {
				GPSManager.this.fragment.locationUpdatesAvailable();
			}
		}

		@Override
		public void onConnectionSuspended(int i) {
			Log.i(TAG, "GoogleApiClient connection has been suspend");
		}
	}
}
