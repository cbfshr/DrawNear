package com.coms309r04.drawnear.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.coms309r04.drawnear.data.DrawingItem;
import com.coms309r04.drawnear.views.DrawingView;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class DrawingManager {

	public enum DisplayType {
		MAP, LIST
	};

	public static final double ISU_LAT = 42.025420, ISU_LNG = -93.646085;
	public static double radius = 0.8;

	/*
	 * Helps to debug drawings out of range int call_count_1 = 0; int
	 * call_count_2 = 0;
	 */

	// updates currentDrawingsList with new drawings from the server (only gives
	// us new drawings)
	// call this method in an AsyncTask
	// also adds new drawing to ArrayList to display if it isn't in it currently
	public void getNearbyDrawings(ParseGeoPoint location, int numberOfDrawings) {

		List<ParseObject> parseObjectList = new ArrayList<ParseObject>();
		List<String> currentDrawingsKeys = new ArrayList<String>();

		// Prepare parameters for function
		HashMap<String, Object> params = new HashMap<String, Object>();
		for (String k : currentDrawings.keySet()) {
			// Logs the keys to see which ones we have
			// Log.w("key:", k);
			currentDrawingsKeys.add(k);
		}

		params.put("myLocation", location);

		/*
		 * Helps to debug drawings out of range if ((call_count_1 % 2) != 0) {
		 * radius = 0.1; } else { radius = 0.8; } call_count_1++;
		 */

		params.put("radius", radius); // radius in km
		params.put("numberOfDrawings", numberOfDrawings);
		params.put("currentDrawings", currentDrawingsKeys);

		try {
			parseObjectList = ParseCloud.callFunction("nearbyDrawings", params);

			Log.i("MAIN", "Retreived from Parse: " + parseObjectList.size() + " drawings within " + radius + " km");

			for (final ParseObject p : parseObjectList) {
				// Log the returned objects
				Log.w("parse:", p.getObjectId());

				final DrawingItem d = new DrawingItem();
				d.setLocation(p.getParseGeoPoint("location"));
				d.setTitle(p.getString("title"));
				d.setId(p.getObjectId());
				d.setCreator(p.getParseUser("creator"));
				d.setDateCreated(p.getCreatedAt());
				d.setEditable(p.getBoolean("editable"));

				if (p.get("privacy") != null) {
					if (p.get("privacy").equals("Public")) {
						d.setPrivacy(DrawingItem.PRIV_TYPE.PUBLIC);
					} else if (p.get("privacy").equals("Friends")) {
						d.setPrivacy(DrawingItem.PRIV_TYPE.FRIENDS);
					} else if (p.get("privacy").equals("Private")) {
						d.setPrivacy(DrawingItem.PRIV_TYPE.PRIVATE);
					} else {
						d.setPrivacy(DrawingItem.PRIV_TYPE.PUBLIC);
					}
				} else {
					d.setPrivacy(DrawingItem.PRIV_TYPE.PUBLIC);
				}

				// load the image to the drawingItem of the location in
				// drawingList you just added
				d.setBitmapFile((ParseFile) p.get("bmp"));

				// Only display drawings that have a valid bitmap
				if (d.getBitmapFile() != null) {
					currentDrawings.put(d.getId(), d);
					this.currentDrawingsList.add(d);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		this.updateDrawingDistances(location);
	}

	private void sortDrawingsListByDistance() {
		if (this.currentDrawingsList.size() > 0) {
			// sort array by drawing distance
			Collections.sort(this.currentDrawingsList,
					new Comparator<DrawingItem>() {
						@Override
						public int compare(DrawingItem d1, DrawingItem d2) {
							return Double.compare(d1.getDistInMiles(),
									d2.getDistInMiles());
						}
					});
		}
	}

	// Removes drawings from list if they are no longer nearby
	public void removeDrawingsNoLongerNearby(ParseGeoPoint location) {

		List<String> drawingKeysToRemove = new ArrayList<String>();
		List<String> currentDrawingsKeys = new ArrayList<String>();

		// Prepare parameters for function
		HashMap<String, Object> params = new HashMap<String, Object>();
		for (String k : this.currentDrawings.keySet()) {
			// Logs the keys to see which ones we have
			Log.i("MAIN", "key added " + k);
			currentDrawingsKeys.add(k);
		}

		params.put("myLocation", location);

		/*
		 * Helps to debug drawings out of range if ((call_count_2 % 2) != 0) {
		 * radius = 0.1; } else { radius = 0.8; } call_count_2++;
		 */

		Log.i("MAIN", "Calling noLongerNearby drawings with: " + radius
				+ " km radius");

		params.put("radius", radius); // radius in km
		params.put("currentDrawings", currentDrawingsKeys);
		params.put("numberOfDrawings", currentDrawingsKeys.size());

		try {
			drawingKeysToRemove = ParseCloud.callFunction(
					"getDrawingsNoLongerNearby", params);

			for (final String s : drawingKeysToRemove) {
				// Log the returned objects
				Log.i("MAIN", "The id " + s + " is no longer nearby");

				// remove drawing that isn't nearby
				this.currentDrawings.remove(s);

				// remove from arraylist as well (use Iterator to allow removal
				// while iterating)
				Iterator<DrawingItem> iter = this.currentDrawingsList
						.iterator();
				while (iter.hasNext()) {
					if (iter.next().getId().equals(s)) {
						iter.remove();
					}
				}
				// May still need to remove markers
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public boolean saveDrawingToRemoteServer(DrawingItem d, final DrawingView drawView) {
		final ParseObject drawing = new ParseObject("DrawingItem");

		drawing.put("location", d.getLocation());
		drawing.put("title", d.getTitle());
		drawing.put("creator", d.getCreator());

		if (d.isEditable())
			drawing.put("editable", true);
		else
			drawing.put("editable", false);

		// Set permissions for who can view and modify the post
		ParseACL acl = new ParseACL();

		// 1) Who can view the Drawing?
		// Uses privacy field of DrawingItem
		// PUBLIC:
		// all can view
		// FRIENDS
		// friends in current user's ParseRole friends can view
		// PRIVATE
		// friends with ParseUser IDs in the drawings recepient list can view
		//
		if (d.getPrivacy() != null) {
			switch (d.getPrivacy()) {
			case PUBLIC:
				drawing.put("privacy", "Public");
				acl.setPublicReadAccess(true);
				if (d.isEditable()) {
					acl.setPublicWriteAccess(true);
				} else {
					acl.setWriteAccess(ParseUser.getCurrentUser(), true);
				}
				break;
			case FRIENDS:
				drawing.put("privacy", "Friends");
				List<ParseUser> friends = (List<ParseUser>) d.getCreator().get("friendsList");
				if (friends != null) {
					acl.setPublicReadAccess(false);
					for (ParseUser p : friends) {
						acl.setReadAccess(p.getObjectId(), true);
						if (d.isEditable()) {
							acl.setWriteAccess(p.getObjectId(), true);
						}
					}
					acl.setReadAccess(d.getCreator(), true);
					acl.setWriteAccess(d.getCreator(), true);
				} else { // if we cannot retrieve friends list, set post to
							// public read
					acl.setPublicReadAccess(true);
					if (d.isEditable()) {
						acl.setPublicWriteAccess(true);
					}
				}
				break;
			case PRIVATE:
				drawing.put("privacy", "Private");
				// TO: the drawing is intended for a list of private recipients
				ArrayList<String> recep = d.getPrivateRecepientIDs();
				if (recep != null) {
					acl.setPublicReadAccess(false);
					for (String id : recep) {
						acl.setReadAccess(id, true);
						if (d.isEditable()) {
							acl.setWriteAccess(id, true);
						}
					}
					acl.setReadAccess(d.getCreator(), true);
					acl.setWriteAccess(d.getCreator(), true);
				} else { // if we cannot retrieve intended recepients list, set
							// post to public read
					acl.setPublicReadAccess(true);
					if (d.isEditable()) {
						acl.setPublicWriteAccess(true);
					}
				}
				break;
			default:
				drawing.put("privacy", "Public");
				acl.setPublicReadAccess(true);
				acl.setWriteAccess(d.getCreator(), true);
				break;
			}
		} else {
			acl.setPublicReadAccess(true);
		}

		// 2) Who can modify the Drawing??
		acl.setWriteAccess(ParseUser.getCurrentUser(), true);
		drawing.setACL(acl);
		

		// Create ParseFile from bmp. This saving process should be improved.
		// Currently, the drawing is not saved until the bmp is saved
		final ParseFile bmpFile = new ParseFile(d.getTitle().toString() + "_"
				+ UUID.randomUUID().toString() + ".bmp", d.getBitmapByteArray());
		bmpFile.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					drawing.put("bmp", bmpFile);
					drawing.saveInBackground();
					drawView.destroyDrawingCache();
				} else {
					e.printStackTrace();
				}
			}
		});

		drawing.saveInBackground();

		return true;
	}

	private void updateDrawingDistances(ParseGeoPoint location) {
		for (DrawingItem drawingItem : currentDrawings.values()) {
			drawingItem.setDistInMiles(drawingItem.getLocation()
					.distanceInMilesTo(location));
		}

		this.sortDrawingsListByDistance();

	}

	public DrawingItem getCurrentDrawingById(String id) {
		return currentDrawings.get(id);
	}

	// Singleton class

	private static DrawingManager instance;
	private HashMap<String, DrawingItem> currentDrawings;

	// This is the list used for the UI in both Nearby and Map views
	final private ArrayList<DrawingItem> currentDrawingsList = new ArrayList<DrawingItem>();

	public static DrawingManager getInstance() {
		if (instance == null)
			instance = new DrawingManager();
		return instance;
	}

	private DrawingManager() {
		this.currentDrawings = new HashMap<String, DrawingItem>();
	}

	public void updateCurrentDrawings(
			HashMap<String, DrawingItem> updatedDrawings) {
		this.currentDrawings = updatedDrawings;
	}

	public void addToCurrentDrawings(HashMap<String, DrawingItem> newDrawings) {
		this.currentDrawings.putAll(newDrawings);
	}

	public ArrayList<DrawingItem> getCurrentDrawingsList() {
		return currentDrawingsList;
	}

	public void removeFromCurrentDrawings(
			HashMap<String, DrawingItem> drawingsToRemove) {
		for (String k : drawingsToRemove.keySet()) {
			if (this.currentDrawings.containsKey(k)) {
				this.currentDrawings.remove(k);
			}
		}
	}

	public HashMap<String, DrawingItem> getCurrentNearbyDrawings() {
		return this.currentDrawings;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

}
