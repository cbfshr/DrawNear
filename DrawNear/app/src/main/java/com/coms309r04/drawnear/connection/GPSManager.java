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
import android.widget.Toast;

public class GPSManager {
    private IGPSActivity main;

    // Helper for GPS-Position
    private MyLocationListener mlocListener;
    private LocationRequest request;
    
    private boolean isRunning;

    private Location lastUpdateLocation = null;
    
    public GPSManager(Context main) {
        this.main = (IGPSActivity)main;

        mlocListener = new MyLocationListener(main);
		request = LocationRequest.create();
		request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		request.setInterval(20000);
		request.setFastestInterval(5000);

        this.isRunning = true;
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
    
    public IGPSActivity getMain() {
		return this.main;
	}

	private class MyLocationListener implements
		ConnectionCallbacks,
		OnConnectionFailedListener,
		LocationListener {

        private final String TAG = MyLocationListener.class.getSimpleName();

        //LocationClient mlocationClient;
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
					GPSManager.this.main.locationChanged(loc.getLatitude(), loc.getLongitude(), distance, true);
				} else {
					GPSManager.this.main.locationChanged(loc.getLatitude(), loc.getLongitude(), distance, false);
				}
			} else {
				lastUpdateLocation = loc;
			}
		}

		@Override
		public void onConnectionFailed(ConnectionResult arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onConnected(Bundle bundle) {
			startLocationUpdates();
			GPSManager.this.main.locationUpdatesAvailable();
		}

		@Override
		public void onConnectionSuspended(int i) {
			// TODO Auto-generated method stub
			Log.i(TAG, "GoogleApiClient connection has been suspend");
		}
    }
}
