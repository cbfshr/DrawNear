package com.coms309r04.drawnear.connection;

public interface ILocationUpdater {
	public void locationChanged(double lat, double lng, float distanceFromLastLocation, boolean updatedLastLocation);

	public void locationUpdatesAvailable();
}
