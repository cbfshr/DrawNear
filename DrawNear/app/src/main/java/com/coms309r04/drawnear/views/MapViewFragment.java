package com.coms309r04.drawnear.views;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// In order for GoogleMaps to work on your machine...
// 1. Add debug key so API key works on your machine
// 2. Add google-play-services_lib as a library project to your application
// ( I copied it into my workspace (it is not in the SVN repo) )
public class MapViewFragment
		extends Fragment
		implements ILocationUpdater, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnInfoWindowClickListener {

	private static final int GPS_ERRORDIALOG_REQUEST = 9001;
	private static final int MENU_GET_CURRENT_LOCATION = 9002;
	private static final int ALLOW_LOCATION_SERVICES = 1;

	@SuppressWarnings("unused")
	private static final double ISU_LAT = 42.025410, ISU_LNG = -93.646085;
	//private static final float DEFAULTZOOM = 14.5f;
	private static final float DEFAULTZOOM = 12f;

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

	ImageButton mapRefresh;
	ObjectAnimator mapRefreshAnimation;

	ImageButton arrowLeft;
	ImageButton arrowRight;
	boolean arrowShown;
	Animation animationFadeIn;
	Animation animationFadeOut;

	GoogleMap mMap;
	Circle radius;
	boolean locationUpdatesAvailable = false;
	// LocationClient mLocationClient;

	private GPSManager gps;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment MapViewFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static MapViewFragment newInstance() {
		MapViewFragment fragment = new MapViewFragment();
		return fragment;
	}

	// Required empty public constructor
	public MapViewFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_map_view, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstance) {
		super.onActivityCreated(savedInstance);

		View view = getView();
		LayoutInflater inflater = getActivity().getLayoutInflater();

		if(servicesOK()) {
			pbMap = (ProgressBar) view.findViewById(R.id.progressBar2);
			pbMap.setVisibility(View.INVISIBLE);

			mapRefresh = (ImageButton) view.findViewById(R.id.map_refresh);
			mapRefreshAnimation = ObjectAnimator.ofFloat(mapRefresh, View.ROTATION, 0, 360);
			mapRefresh.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onRefresh();
				}
			});

			arrowLeft = (ImageButton) view.findViewById(R.id.map_arrow_left);
			arrowLeft.setVisibility(View.INVISIBLE);
			arrowLeft.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onArrowClick(v);
				}
			});

			arrowRight = (ImageButton) view.findViewById(R.id.map_arrow_right);
			arrowRight.setVisibility(View.INVISIBLE);
			arrowRight.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onArrowClick(v);
				}
			});

			animationFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
			animationFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fadeout);

			if(initMap(inflater)) {
				if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					ActivityCompat.requestPermissions(
						getActivity(),
						new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
						ALLOW_LOCATION_SERVICES
					);
				} else {
					resetMap();
				}

				// mLocationClient = new LocationClient(this, this, this);
				// mLocationClient.connect();
			} else {
				Toast.makeText(getActivity(), "Map not available!", Toast.LENGTH_SHORT).show();
			}
		} else {
			//setContentView(R.layout.activity_map_no_map);
			Toast.makeText(getActivity(), "Map is not available", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		resetMap();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private void resetMap() {
		// This should be created in the activity and passed to fragments
		gps = new GPSManager(getActivity(), this);

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


	public void loadAndUpdateDrawings() {
		// Get nearby drawings
		if (gps != null && gps.getLastLocation() == null) {
			// This Toast is shown a lot - not sure why
			//Toast.makeText(getActivity(), "Map: Can't get last location", Toast.LENGTH_SHORT).show();
		}

		/*Toast.makeText(this, "Checking for new drawings...", Toast.LENGTH_SHORT).show();*/
		MyMapTask getDrawingsThread = new MyMapTask();
		getDrawingsThread.execute();
	}

	public boolean servicesOK() {
		int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
		if (isAvailable == ConnectionResult.SUCCESS) {
			return true;
		} else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, getActivity(), GPS_ERRORDIALOG_REQUEST);
			dialog.show();
		} else {
			Toast.makeText(getActivity(), "Can't connect to Google Play services", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	private boolean initMap(LayoutInflater inflater) {
		final LayoutInflater mapViewInflater = inflater;

		if (mMap == null) {
			mMap = getMapFragment().getMap();

			if (mMap != null) {
				mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
					@Override
					public View getInfoWindow(Marker arg0) {
						return null;
					}

					@Override
					public View getInfoContents(Marker marker) {
						View v = mapViewInflater.inflate(R.layout.map_info_window, null);
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

				mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
				mMap.setOnMapClickListener((GoogleMap.OnMapClickListener) this);
				mMap.setOnInfoWindowClickListener((GoogleMap.OnInfoWindowClickListener) this);
			}
		}
		return (mMap != null);
	}

	// The method for retriving a MapFragment is different depending on the version
	// Solution: http://stackoverflow.com/questions/26592889/mapfragment-or-mapview-getmap-returns-null-on-lollipop/27681586#27681586
	private MapFragment getMapFragment() {
		android.app.FragmentManager fragmentManager = null;

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			fragmentManager = getFragmentManager();
		} else {
			fragmentManager = getChildFragmentManager();
		}

		return (MapFragment) fragmentManager.findFragmentById(R.id.mapView);
	}

	protected void gotoCurrentLocationDefaultZoom() {
		ParseGeoPoint currentLocation = null;
		if(gps != null) {
			currentLocation = gps.getLastLocation();
		}
		if(currentLocation == null) {
			// This Toast is shown a lot - not sure why
			//Toast.makeText(getActivity(), "Map: Current location isn't available", Toast.LENGTH_SHORT).show();
		} else {
			LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULTZOOM);
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

		LatLng latLng = new LatLng(lat, lng);

		CircleOptions options = new CircleOptions().center(latLng).radius(r).fillColor(0x11FF00FF).strokeColor(Color.BLUE).strokeWidth(3);
		radius = mMap.addCircle(options);
	}

	private void drawMarkers() {
		for(DrawingItem drawing : DrawingManager.getInstance().getCurrentNearbyDrawings().values()) {
			// only create a marker for a drawing if it has not already been created
			if(!(markerIDsToDrawingsIDs.values().contains(drawing.getId()))) {
				MarkerOptions options = new MarkerOptions()
					.title(drawing.getTitle())
					.position(
							new LatLng(
									drawing.getLocation().getLatitude(),
									drawing.getLocation().getLongitude()
							)
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
		}// else {
			/*Toast.makeText(this, "Not far enough from last location to update drawings.", Toast.LENGTH_SHORT).show();*/
		//}
	}

	@Override
	public void locationUpdatesAvailable() {
		Toast.makeText(getActivity(), "Map: Connected to location services", Toast.LENGTH_LONG).show();
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
				Toast.makeText(getActivity(), "Map: Could not connect to receive drawings", Toast.LENGTH_SHORT).show();
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


	public void onRefresh() {
		resetMap();
		mapRefreshAnimation.start();
	}

	private void enableArrows(boolean b) {
		if(b) {
			if(!arrowShown) {
				arrowLeft.setVisibility(View.VISIBLE);
				arrowRight.setVisibility(View.VISIBLE);
				arrowLeft.startAnimation(animationFadeIn);
				arrowRight.startAnimation(animationFadeIn);

				arrowShown = true;
			}
		} else {
			if(arrowShown) {
				arrowLeft.startAnimation(animationFadeOut);
				arrowRight.startAnimation(animationFadeOut);
				arrowLeft.setVisibility(View.INVISIBLE);
				arrowRight.setVisibility(View.INVISIBLE);

				arrowShown = false;
			}
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
		Intent intent = new Intent(getActivity(), DisplayPostActivity.class);
		intent.putExtra("id", markerIDsToDrawingsIDs.get(m.getId()));
		startActivity(intent);
	}
}
