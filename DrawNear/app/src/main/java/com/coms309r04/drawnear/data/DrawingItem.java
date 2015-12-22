package com.coms309r04.drawnear.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.android.gms.maps.model.Marker;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseRole;
import com.parse.ParseUser;

import android.graphics.Bitmap;

public class DrawingItem {
	public enum PRIV_TYPE{
		PUBLIC, FRIENDS, PRIVATE
	}
	
	private String title;
	private String id;

	ParseGeoPoint location;
	private int rating;

	private Bitmap bmp;
	private Bitmap thumbnail;

	private ParseFile bitmapFile;

	private byte[] bitmapByteArray;

	private PRIV_TYPE privacy;
	private boolean editable;
	
	//Recepient(s) if the post is private
	private ArrayList<String> privateRecepientIDs;

	private ParseUser creator;
	private Date dateCreated;

	private double distInMiles;
	private String markerId;

	public ArrayList<String> getPrivateRecepientIDs() {
		return privateRecepientIDs;
	}

	public void setPrivateRecepients(ArrayList<String> privateRecepientIDs) {
		this.privateRecepientIDs = privateRecepientIDs;
	}	
	
	public PRIV_TYPE getPrivacy() {
		return privacy;
	}

	public void setPrivacy(PRIV_TYPE privacy) {
		this.privacy = privacy;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ParseGeoPoint getLocation() {
		return location;
	}

	public void setLocation(ParseGeoPoint location) {
		this.location = location;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public Bitmap getBmp() {
		return bmp;
	}

	public void setBmp(Bitmap bmp) {
		this.bmp = bmp;
	}

	public double getDistInMiles() {
		return distInMiles;
	}

	public void setDistInMiles(double distInMiles) {
		this.distInMiles = distInMiles;
	}

	public ParseFile getBitmapFile() {
		return bitmapFile;
	}

	public void setBitmapFile(ParseFile bitmapFile) {
		this.bitmapFile = bitmapFile;
	}

	public byte[] getBitmapByteArray() {
		return bitmapByteArray;
	}

	public void setBitmapByteArray(byte[] bitmapByteArray) {
		this.bitmapByteArray = bitmapByteArray;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public ParseUser getCreator() {
		return creator;
	}

	public void setCreator(ParseUser creator) {
		this.creator = creator;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date date) {
		this.dateCreated = date;
	}

	public void setMarkerId(String markerId) {
		this.markerId = markerId;
	}

	public void setThumbnail(Bitmap thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	public Bitmap getThumbnail() {
		return thumbnail;
	}

}

// import java.util.Date;
//
// public class DrawingItem {
// //create getter/setter functions for these and make them private
// private int distance;
// private String strDistance;
//
// public String editable;
// public String dateCreated;
// public String dateExpiry;
//
// /*
// //private UserItem creator;
// private String creator;
// //private UserItem[] contributingEditors;
// private String[] contibutingEditors;
// private int rating;
// //private ViewableLevel viewableLevel;
// */
//
// public DrawingItem (int distance, boolean editable, Date dateCreated, Date
// dateExpiry) {
// setDistance(distance);
// //this.distance = "Distance: " +distance +" meters";
// setStrDistance(this.getDistance());
//
// if(editable)
// this.editable = "Editable: Yes";
// else
// this.editable = "Editable: No";
//
// this.dateCreated = dateCreated.toString();
//
// this.dateExpiry = dateExpiry.toString();
// }
//
//
// public void setDistance (int distance) {
// if(distance < 0) distance = 0;
// this.distance = distance;
// }
//
// public int getDistance() {
// return this.distance;
// }
//
// public void setStrDistance(int distance) {
// if(distance < 0) distance = 0;
// this.strDistance = "Distance: " +distance +" meters";
// }
//
// public String getStrDistance() {
// return this.strDistance;
// }
//
//
// // Properties of a DrawingItem:
// // original artist
// /*public void setCreator(String creator) {
// this.creator = creator;
// }*/
// // contributing artists
// // rating (upvotes/downvotes)
// // date created
// // image (.bmp?)
// // editable? (yes/no)
// // viewable by: (public, friends, certain recepients only?)
// // suprise drawing? (boolean) (this determines if intended recipients see
// // the location of the drawing before they are in range of it)
// // expiration time
// }
