package com.coms309r04.drawnear.connection;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
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
    
    public GPSManager(IGPSActivity main) {
        this.main = main;

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
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{

        private final String TAG = MyLocationListener.class.getSimpleName();

        LocationClient mlocationClient;
        
        public MyLocationListener(IGPSActivity main) {
			mlocationClient = new LocationClient( (Context) main, this, this );
			mlocationClient.connect();
        }

		public void stopLocationUpdates() {
			mlocationClient.removeLocationUpdates(this);
		}
		
		public void startLocationUpdates() {
			mlocationClient.requestLocationUpdates(request, this);	
		}
		
		public Location getLastLocation(){
			return mlocationClient.getLastLocation();
			
		}

		@Override
		public void onLocationChanged(Location loc) {
			if(lastUpdateLocation != null) {
				float distance = loc.distanceTo(lastUpdateLocation);
				if (distance > 20.0) { //20 meters
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
		public void onDisconnected() {
			// TODO Auto-generated method stub
			
		}


    }

}