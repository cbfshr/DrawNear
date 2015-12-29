package com.coms309r04.drawnear.data;

import java.util.ArrayList;
import java.util.Date;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import android.graphics.Bitmap;

public class DrawingItem {
	public enum PRIVACY_TYPE {
		PUBLIC,
		FRIENDS,
		PRIVATE
	}

	private String title;
	private String id;

	ParseGeoPoint location;
	private int rating;

	private Bitmap bmp;
	private Bitmap thumbnail;

	private ParseFile bitmapFile;

	private byte[] bitmapByteArray;

	private PRIVACY_TYPE privacy;
	private boolean editable;

	//Recipient(s) if the post is private
	private ArrayList<String> privateRecipientIDs;

	private ParseUser creator;
	private Date dateCreated;

	private double distInMiles;
	private String markerId;

	public ArrayList<String> getPrivateRecipientIDs() {
		return privateRecipientIDs;
	}

	public void setPrivateRecipients(ArrayList<String> privateRecepientIDs) {
		this.privateRecipientIDs = privateRecepientIDs;
	}

	public PRIVACY_TYPE getPrivacy() {
		return privacy;
	}

	public void setPrivacy(PRIVACY_TYPE privacy) {
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

	public double getDistanceInMiles() {
		return distInMiles;
	}

	public void setDistanceInMiles(double distInMiles) {
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
